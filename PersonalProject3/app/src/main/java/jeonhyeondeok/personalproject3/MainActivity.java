package jeonhyeondeok.personalproject3;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView list_music;
    TextView tv_name, tv_state;
    ProgressBar progressBar;

    ArrayList<MusicItem> musicList;
    MusicAdapter musicAdapter;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicList = new ArrayList<>();

        list_music = (RecyclerView)findViewById(R.id.list_music);
        tv_name = (TextView)findViewById(R.id.tv_name);
        tv_state = (TextView)findViewById(R.id.tv_state);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        musicAdapter = new MusicAdapter();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list_music.setLayoutManager(linearLayoutManager);

        // 인터넷 접속해서 리스트 획득하여 musicList 에 저장.
        new HttpTask().execute("list");

        list_music.setAdapter(musicAdapter);

        mediaPlayer = new MediaPlayer();
    }

    /**
     * MusicAdapter.
     * 리사이클러 뷰 용 커스텀 어댑터
     */
    public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{


        public class MusicViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name;

            public MusicViewHolder(View itemView) {
                super(itemView);
                tv_name = (TextView)itemView.findViewById(R.id.tv_name);

                // 곡 제목을 클릭할 경우 곡을 내려받아 재생
                tv_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new HttpTask().execute("music", tv_name.getText().toString());
                    }
                });
            }
        }

        @Override
        public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_music, parent, false);
            return new MusicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MusicViewHolder holder, int position) {
            holder.tv_name.setText(musicList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return musicList.size();
        }
    }
    public class MusicItem{
        String name;

        public MusicItem(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * HttpTask.
     * 실제 인터넷 동작 부분을 담당하는 클래스.
     * 곡 목록 획득과 음악파일 획득의 두가지 기능을 담당한다
     */
    public class HttpTask extends AsyncTask<String, Integer, byte[]>{

        private final static int GET_LIST = 0;
        private final static int GET_MUSIC = 1;

        private int mode;
        private String name;
        @Override
        protected byte[] doInBackground(String... strings) {

            byte[] bytes = null;
            try {
                URL url = null;
                // 첫 번째 인자값으로 동작 설정
                if(strings[0].equals("list")){
                    mode = GET_LIST;
                    url = new URL("http://45.32.31.3:8011/music/list.php?mode=" + strings[0]);
                }else if(strings[0].equals("music")){
                    mode = GET_MUSIC;
                    name = strings[1];

                    // url 에서 깨질 수 있는 부분(곡 제목)을 인코딩하여 처리
                    url = new URL("http://45.32.31.3:8011/music/" + URLEncoder.encode(name, "utf-8").replace("+", "%20") + ".mp3");
                }

                // 인터넷에 접속하여 byte 형태로 데이터 획득
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    byte[] data = new byte[1024];
                    long totalLength = urlConnection.getContentLength();    // 전체 크기
                    long total = 0;     // 받은 크기
                    long read_count;    // 읽어온 버퍼 크기

                    ByteBuffer byteBuffer = ByteBuffer.allocate((int)totalLength);

                    // 1024바이트 단위로 반복해서 모든 데이터를 읽어 byte 버퍼에 저장
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    while((read_count = in.read(data)) != -1){
                        total += read_count;
                        publishProgress((int)((total * 100) / totalLength));
                        byteBuffer.put(data, 0, (int)read_count);
                    }
                    in.close();

                    bytes = byteBuffer.array();
                    return bytes;

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return bytes;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(mode == GET_MUSIC) {
                tv_name.setText(name);

                tv_state.setText("다운로드 중...");
                progressBar.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            // 도중에 문제가 생긴 경우 처리 생략
            if(bytes == null) {
                tv_state.setText("오류 발생");
                return;
            }

            if(mode == GET_LIST){
                // 곡 목록에 데이터 입력
                musicList.clear();

                String str = new String(bytes);
                String[] strSongList = str.split("\n");
                for(String songName : strSongList){
                    musicList.add(new MusicItem(songName));
                }

                musicAdapter.notifyDataSetChanged();

            }else if(mode == GET_MUSIC){
                try {
                    mediaPlayer.reset();

                    File path = new File(getCacheDir() + "/temp.mp3");

                    FileOutputStream fos = new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();

                    mediaPlayer.setDataSource(getCacheDir() + "/temp.mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();

                    tv_state.setText("재생 중...");

                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }
    }
}
