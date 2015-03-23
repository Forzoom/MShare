package org.mshare.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.mshare.file.browser.FileBrowserFile;

import android.content.Context;
import android.widget.Toast;

public class MshareFileManage {
	//判断是否有文件被选中复制或剪切
	private boolean selected = false;
	private FileBrowserFile[] file = null;
	private Context context = null;
	private boolean cut = false;
	private int pasteCount = 0;
	
	public MshareFileManage(Context c) {
		context = c;
	}
	
	//获取cut的值
	public boolean getCut() {
		return cut;
	}
	
	//获取select的值
	public boolean getSelect() {
		return selected;
	}
	
	//复制剪切选择
	public void copySelect(FileBrowserFile[] f, boolean c) {
		selected = true;
		file = f;
		cut = c;
		Toast.makeText(context, "请选择目标文件夹", Toast.LENGTH_SHORT).show();
	}
	
	//取消粘贴
	public void pasteCancel() {
		selected = false;
		file = null;
	}
	
	//剪切多个文件
	public void moveMultiFiles(String path) {
		int length = this.file.length;
		for(int i = 0; i < length; i++) {
			String oldPath = this.file[i].getAbsolutePath();
			String newPath = path + File.separator + this.file[i].getName();
			moveFile(oldPath, newPath);
		}
		if(this.pasteCount == length) {
			Toast.makeText(context, "文件全部剪切成功", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, String.valueOf(length-this.pasteCount) + "个文件剪切出错", Toast.LENGTH_SHORT).show();
		}
		this.pasteCount = 0;
		pasteCancel();
	}
	
	//剪切
	private void moveFile(String oldPath, String newPath) {
		if(new File(newPath).exists()) {
			Toast.makeText(context, "剪切出错", Toast.LENGTH_SHORT).show();
		}
		else {
			new File(oldPath).renameTo(new File(newPath));
			this.pasteCount++;
		}
	}
	
	//复制多个文件
	public void CopyMultiFiles(String path) {
		int length = this.file.length;
		for(int i = 0; i < length; i++) {
			String oldPath = this.file[i].getAbsolutePath();
			String newPath = path + File.separator + this.file[i].getName();
			pasteOfCopy(oldPath, newPath);
		}
		if(this.pasteCount == length) {
			Toast.makeText(context, "文件全部复制成功", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, String.valueOf(length-this.pasteCount) + "个文件复制出错", Toast.LENGTH_SHORT).show();
		}
		this.pasteCount = 0;
		pasteCancel();
	}
	
	//粘贴-复制
	private void pasteOfCopy(String fromPath, String toPath) {
		if(selected) {
			if(toPath.equals(fromPath) || new File(toPath).exists()) {
				Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show();
			}
			else {
				if(copy(fromPath, toPath)) {
					this.pasteCount++;
				}
				else {
					Toast.makeText(context, "复制出错", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			pasteCancel();
		}
	}
	
	//复制文件
	private boolean copy(String fromPath, String toPath) {
		File file = new File(fromPath);
		if(file.isFile()) {
			return copyFile(fromPath, toPath);
		}
		else {
			return copyFolder(fromPath, toPath);
		}
	}
	
	//复制单个文件
	public boolean copyFile(String fromPath, String toPath) { 
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
	private boolean copyFolder(String fromPath, String toPath) { 
	   boolean isok = true;
       try { 
           (new File(fromPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹 
           File a=new File(fromPath); 
           (new File(toPath)).mkdirs();
           String[] file=a.list(); 
           File temp=null; 
           
           for (int i = 0; i < file.length; i++) { 
               if(fromPath.endsWith(File.separator)){ 
                   temp=new File(fromPath+file[i]); 
               } 
               else
               { 
                   temp=new File(fromPath+File.separator+file[i]); 
               } 

               if(temp.isFile()){ 
            	   
                   FileInputStream input = new FileInputStream(temp); 
                   FileOutputStream output = new FileOutputStream(toPath + "/" + 
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
                   copyFolder(fromPath+"/"+file[i],toPath+"/"+file[i]); 
               } 
           } 
       } 
       catch (Exception e) { 
    	   isok = false;
       } 
       return isok;
	}	
	
	//删除整个文件或文件夹
	private boolean deleteAll(String path) {
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
	
	//删除文件和文件夹里内容
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
	
	//删除多个文件或文件夹
	public void deleteMultiFiles(FileBrowserFile[] f) {
		int length = f.length;
		for(int i = 0; i < length; i++) {
			deleteAll(f[i].getAbsolutePath());
		}
	}
	
	//重命名文件
	public void renameFile(String oldPath, String newPath) {
		File file = new File(oldPath);
		file.renameTo(new File(newPath));
	}
	
	//新建文件夹
	public void newFolder(String path) {
		(new File(path)).mkdirs();
	}
}
