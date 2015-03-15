package org.mshare.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.widget.Toast;

public class MshareFileManage {
	//判断是否有文件被选中复制或剪切
	private boolean selected = false;
	private String fromPath = null;
	private String toPath = null;
	private String name = null;
	private Context context = null;
	private boolean cut = false;
	
	//设置context
	public void setContext(Context c) {
		context = c;
	}
	
	//获取select的值
	public boolean getSelected() {
		return selected;
	}
	
	//复制剪切选择
	public void copySelect(String fp, String n, boolean c) {
		selected = true;
		fromPath = fp;
		name = n;
		cut = c;
		Toast.makeText(context, "请选择目标文件夹", Toast.LENGTH_SHORT).show();
	}
	
	//取消粘贴
	public void copyCancel() {
		selected = false;
		fromPath = null;
	}
	
	//粘贴
	public void paste(String tp) {
		if(selected) {
			toPath = tp + "/" + name;
			if(toPath.equals(fromPath)) {
				Toast.makeText(context, "粘贴失败", Toast.LENGTH_SHORT).show();
			}
			else {
				if(copy()) {
					Toast.makeText(context, "粘贴成功", Toast.LENGTH_SHORT).show();
					if(cut) {
						deleteAll(fromPath);
					}
					copyCancel();
				}
				else {
					Toast.makeText(context, "粘贴出错", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			copyCancel();
		}
	}
	
	//复制文件
	private boolean copy() {
		File file = new File(fromPath);
		if(file.isFile()) {
			return copyFile();
		}
		else {
			return copyFolder(fromPath, toPath);
		}
	}
	
	//复制单个文件
	public boolean copyFile() { 
	   boolean isok = true;
       try { 
           int bytesum = 0; 
           int byteread = 0; 
           File oldfile = new File(fromPath); 
           if (oldfile.exists()) { //文件不存在时 
               InputStream inStream = new FileInputStream(fromPath); //读入原文件 
               FileOutputStream fs = new FileOutputStream(toPath); 
               byte[] buffer = new byte[1024]; 
               int length; 
               while ( (byteread = inStream.read(buffer)) != -1) { 
                   bytesum += byteread; //字节数 文件大小 
                   //System.out.println(bytesum); 
                   fs.write(buffer, 0, byteread); 
               } 
               fs.flush(); 
               fs.close(); 
               inStream.close(); 
           }
           else
           {
			isok = false;
		   }
       } 
       catch (Exception e) { 
          // System.out.println("复制单个文件操作出错"); 
          // e.printStackTrace(); 
           isok = false;
       } 
       return isok;
	} 
	
	//复制整个文件夹
	private boolean copyFolder(String oldPath, String newPath) { 
	   boolean isok = true;
       try { 
           (new File(oldPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹 
           File a=new File(oldPath); 
           (new File(newPath)).mkdirs();
           String[] file=a.list(); 
           File temp=null; 
           
           for (int i = 0; i < file.length; i++) { 
               if(oldPath.endsWith(File.separator)){ 
                   temp=new File(oldPath+file[i]); 
               } 
               else
               { 
                   temp=new File(oldPath+File.separator+file[i]); 
               } 

               if(temp.isFile()){ 
            	   
                   FileInputStream input = new FileInputStream(temp); 
                   FileOutputStream output = new FileOutputStream(newPath + "/" + 
                           (temp.getName()).toString()); 
                   
                   byte[] b = new byte[1024]; 
                   
                   int len; 
                   while ( (len = input.read(b)) != -1) { 
                       output.write(b, 0, len); 
                   }
                   output.close(); 
                   input.close(); 
                   
               } 
               if(temp.isDirectory()){//如果是子文件夹 
                   copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]); 
               } 
           } 
       } 
       catch (Exception e) { 
    	   isok = false;
       } 
       return isok;
	}	
	
	public boolean deleteAll(String path) {
		boolean isok = true;
		try {
			File file = new File(path);
			if(file.isDirectory()) {
				deleteFiles(path);//清空文件夹内容
			}
			file.delete();
		}
		catch(Exception e) {
			isok = false;
		}
		return isok;
	}
	
	private boolean deleteFiles(String path) {
		boolean isok = true;
		try {
			File f = new File(path);
			String[] files = f.list();
			for(int i = 0; i < files.length; i++) {
				File temp = new File(path + File.separator + files[i]);
				if(temp.isFile()) {
					temp.delete();
				}
				else {
					deleteAll(path + File.separator + files[i]);
				}
			}
		}
		catch(Exception e) {
			isok = false;
		}
		return isok;
	}
}
