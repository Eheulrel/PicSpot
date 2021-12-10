package net.codestorage.picspot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SingleAdapter extends BaseAdapter {
    private static final String TAG = "SingleAdapter";

    private ArrayList<locate> arrayList;
    private Context context;

    public SingleAdapter(ArrayList<locate> arrayList, Context context){
        this.arrayList = arrayList;
        this.context = context;
    }

    //최초 화면의 갯수를 설정함
    @Override
    public int getCount() {
        Log.d(TAG, "getCount: "+ arrayList.size());
        return arrayList.size();
    }

    //아이템이 클릭될 때 아이템의 데이터를 도출
    @Override
    public Object getItem(int position) {
        Log.d(TAG, "getItem: ");
        return arrayList.get(position);
    }

    //아이템아이디 도출
    @Override
    public long getItemId(int position) {
        Log.d(TAG, "getItemId: "+position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getVIew :"+position);
        //레이아웃 인플레이터로 인플레이터 객체 접근
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //메모리에 아이템 하나 인플레이팅
        View itemView = inflater.inflate(R.layout.listview_item, parent, false);
        ///뷰 찾기
        TextView textView = itemView.findViewById(R.id.placeTitle);
        ImageView imageView = itemView.findViewById(R.id.iv_img_resource);
        //뷰 교체
        String title = arrayList.get(position).getTitle();
        Bitmap imgResource = StringToBitmap(arrayList.get(position).getImgResource());
        textView.setText(title);
        imageView.setImageBitmap(imgResource);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,SpotinfoActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);

                Bitmap finalBitmap = imgResource;

                //갤러리에서 선택한 사진 Intent
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra("bitmap", byteArray);

                context.startActivity(intent);
                ((BookmarkActivity)context).finish();
            }
        });

        return itemView;
    }

    //String -> Bitmap
    public static Bitmap StringToBitmap(String encodedString){
        try{
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
