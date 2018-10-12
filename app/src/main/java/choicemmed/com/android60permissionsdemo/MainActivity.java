package choicemmed.com.android60permissionsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author zhengzhong on 2016/8/6 16:16
 * Email zheng_zhong@163.com
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    @ViewInject(R.id.photo)
    private CircleImageView photo;  //圆形头像
    @ViewInject(R.id.takePic)
    private Button takePic;  //打开相机
    @ViewInject(R.id.takeGallery)
    private Button takeGallery;    //打开相册
    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_RESULT_REQUEST = 0xa2;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x04;
    /*\
    Environment.getExternalStorageDirectory().getPath()
    环境 。 获取 外部 存储 目录 。 获取 路径

    goole官方文档中的解释:
    不要被“外部”这个词弄糊涂了。
    这个目录可以更好地被认为是媒体/共享存储。它是一个文件系统，可以保存相对大量的数据，并在所有应用程序之间共享（不强制执行权限）。
    传统上，这是SD卡，但它也可以被实现为与被保护的内部存储区不同的设备中的内置存储器，并且可以作为计算机上的文件系统安装。

    获取外置sd卡路径或获取默认内置存储器的路径。
     */

    /*
    获得外部存储路径
     */
    private File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");    //拍照后图片途径
    private File fileCropUri = new File(Environment.getExternalStorageDirectory().getPath() + "/crop_photo.jpg");   //裁剪后图片路径
    private Uri imageUri;
    private Uri cropImageUri;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("路径1",Environment.getExternalStorageDirectory()+"");      //文件类型路径
        Log.i("路径2",Environment.getExternalStorageDirectory().getPath());   //字符串类型路径

        /*
        xUtils 工具包
        ViewUtils使用方法:
完全注解方式就可以进行UI绑定和事件绑定。
无需findViewById和setClickListener等
         */
        ViewUtils.inject(this);
    }


    @OnClick({R.id.takePic, R.id.takeGallery})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePic:
                autoObtainCameraPermission();   //获取相机权限调用相机
                break;
            case R.id.takeGallery:
                autoObtainStoragePermission();  //获取相册权限打开相册
                break;
            default:
        }
    }

    /**
     * 自动获取相机权限
     */
    private void autoObtainCameraPermission() {

        //判断是否有相机权限与读取外部存储器权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            //如果上次点击过取消赋予相机权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ToastUtils.showShort(this, "您已经拒绝过一次");
            }
            //再次请求相机权限，读取外部存储器权限  并有请求结果码
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
        } else {//有权限直接调用系统相机拍照
            //检查设备是否存在SDCard的工具方法
            if (hasSdcard()) {
                //获取拍照相片的路径(文件变为文件地址)
                imageUri = Uri.fromFile(fileUri);
                //7.0以上通过FileProvider.getUriForFile获取文件路径
                //通过FileProvider创建一个content类型的Uri

                Log.i("233根据Uri得到路径",imageUri.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "com.zz.fileprovider", fileUri);

                    Log.i("233根据Uri得到路径",imageUri.toString());

                }
                //隐式意图打开相机类应用，并将拍摄照片存入sd卡或内部存储器+photo.jpg       请求码
                PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
            } else {
                ToastUtils.showShort(this, "设备没有SD卡！");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //调用系统相机申请拍照权限回调
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (hasSdcard()) {
                        imageUri = Uri.fromFile(fileUri);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.zz.fileprovider", fileUri);//通过FileProvider创建一个content类型的Uri
                        PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
                    } else {
                        ToastUtils.showShort(this, "设备没有SD卡！");
                    }
                } else {

                    ToastUtils.showShort(this, "请允许打开相机！！");
                }
                break;


            }
            //调用系统相册申请Sdcard权限回调
            case STORAGE_PERMISSIONS_REQUEST_CODE:
                //如果权限组第一个权限请求成功
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //打开相册类应用
                    PhotoUtils.openPic(this, CODE_GALLERY_REQUEST);
                } else {
                    ToastUtils.showShort(this, "请允许打操作SDCard！！");
                }
                break;
            default:
        }
    }

    private static final int OUTPUT_X = 480;
    private static final int OUTPUT_Y = 480;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //拍照完成回调
                case CODE_CAMERA_REQUEST:
                    //获取裁剪后的图片路径
                    cropImageUri = Uri.fromFile(fileCropUri);
                    //调用系统裁剪图片
                    // 原图片途径
                    // 裁剪后存储路径
                    /*
                    @param aspectX     X方向的比例
                    Y方向的比例
                     剪裁图片的宽度
                    剪裁图片高度
                     剪裁图片的请求码
                     */
                    PhotoUtils.cropImageUri(this, imageUri, cropImageUri, 1, 1, OUTPUT_X, OUTPUT_Y, CODE_RESULT_REQUEST);
                    break;
                //访问相册完成回调
                case CODE_GALLERY_REQUEST:
                    //有内部存储器
                    if (hasSdcard()) {
                        //获取图片文件(photo.jpg)的uri
                        cropImageUri = Uri.fromFile(fileCropUri);
                        Log.i("3",data.getData()+"");       // content://com.android.providers.media.documents/document/image%3A988959
                        Log.i("4",PhotoUtils.getPath(this, data.getData()));    // file:////storage/emulated/0/Pictures/Screenshots/Screenshot_20180722-210955.png 字符转类型路径
                        Log.i("5",Uri.parse(PhotoUtils.getPath(this, data.getData()))+"");  // file:////storage/emulated/0/Pictures/Screenshots/Screenshot_20180722-210955.png   Uri类型路径

                        //文件地址转换为Uri
                        //创建一个解析给定URI字符串的URI。
                        Uri newUri = Uri.parse(PhotoUtils.getPath(this, data.getData()));

                        Log.i("233相机Uri得到路径",newUri.toString());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //24
                            newUri = FileProvider.getUriForFile(this, "com.zz.fileprovider", new File(newUri.getPath()));

                            Log.i("233相机Uri得到路径",newUri.toString());


                            /*
                            检索文件的MIME类型
文件的数据类型向客户端应用程序指示它应如何处理文件的内容。为了获得共享文件的数据类型给定其内容URI，
客户端应用程序调用 ContentResolver.getType()。
此方法返回文件的MIME类型。默认情况下，a FileProvider从文件扩展名确定文件的MIME类型。
                             */
                            String mimeType = getContentResolver().getType(newUri);

                            Log.i("233相机Uri得到文件类型",mimeType);



                            /*
                            检索文件的名称和大小
本FileProvider类有一个默认的实现 query()是返回与一个内容URI关联的文件的名称和大小的方法 Cursor。默认实现返回两列：

DISPLAY_NAME
文件的名称，作为String。此值与返回的值相同File.getName()。
SIZE
文件的大小（以字节为单位），作为long此值与返回的值相同File.length()
客户端应用程序可以同时获得DISPLAY_NAME，并SIZE通过设置所有的参数文件query()到 null除了内容URI。
                             */

                            Cursor returnCursor =
                                    getContentResolver().query(newUri, null, null, null, null);
    /*
     * 从传入的Intent获取文件的内容URI，     *然后查询服务器应用程序以获取文件的显示名称     *和大小。
     */
                            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);

                            Log.i("233相机中文件名和大小","名"+nameIndex+"大小"+sizeIndex);
                            returnCursor.moveToFirst();

                        }
                        //裁剪图片 请求码
                        PhotoUtils.cropImageUri(this, newUri, cropImageUri, 1, 1, OUTPUT_X, OUTPUT_Y, CODE_RESULT_REQUEST);
                    } else {
                        ToastUtils.showShort(this, "设备没有SD卡！");
                    }
                    break;
                case CODE_RESULT_REQUEST:
                    //将裁剪后图片路径装bitmap对象
                    //Bitmap bitmap = PhotoUtils.getBitmapFromUri(cropImageUri, this);

                    //将裁剪后的图片路径(uri)压缩后装bitmap对象
                    String str_uri = ImageUtils.getImagePathFromImageUri(this,cropImageUri);
                    Bitmap bitmap =ImageUtils.getYaSuoBitmapFromImagePath(str_uri,0,0);

                    if (bitmap != null) {
                        //显示图片
                        showImages(bitmap);
                    }
                    break;
                default:
            }
        }
    }


    /**
     * 自动获取sdk权限
     */

    private void autoObtainStoragePermission() {
        //判断是否有读取外部存储器的权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求读取外部存储器的权限 请求码
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            //打开相册类型应用
            PhotoUtils.openPic(this, CODE_GALLERY_REQUEST);
        }

    }

    private void showImages(Bitmap bitmap) {
        photo.setImageBitmap(bitmap);
    }

    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        //获取外部存储器状态
        String state = Environment.getExternalStorageState();
        //返回设备是否安装
        return state.equals(Environment.MEDIA_MOUNTED);
    }


}
