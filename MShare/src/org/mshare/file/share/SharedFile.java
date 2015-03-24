package org.mshare.file.share;

import java.io.File;

import android.util.Log;

// 所需要的内容 realPath和fakePath
public class SharedFile extends SharedLink {
	private static final String TAG = SharedFile.class.getSimpleName();
	
	@Override
	public boolean isFile() {
		return true;
	}
	@Override
	public boolean isDirectory() {
		return false;
	}
	@Override
	public boolean isFakeDirectory() {
		return false;
	}

	@Override
	public SharedLink[] listFiles() {
		return null;
	}

	@Override
	public long length() {
		return getRealFile().length();
	}

	@Override
	public boolean canRead() {
		return super.canRead() && getRealFile().canRead();
	}

	@Override
	public boolean canWrite() {
		return super.canWrite() && getRealFile().canWrite();
	}
	
	@Override
	public long lastModified() {
		return getRealFile().lastModified();
	}

	/**
	 * 删除一个文件，需要对存储文件的内容进行处理
	 * 并不是真正地删除一个文件
	 * 是否有必要:实现异步的删除
	 * 删除文件 删除持久化 删除文件树  
	 */
	@Override
	public boolean delete() {
		
		// 这里需要判断用户的权限吗
		if (!canWrite()) {
			Log.e(TAG, "用户没有权限执行删除操作");
			return false;
		}
		
		File file = getRealFile();
		if (!file.exists()) {
			Log.e(TAG, "需要删除的原文件不存在");
			return false;
		}
		if (file.isDirectory()) {
			Log.e(TAG, "不能删除一个文件夹");
			// TODO 需要修正现在的显示内容，还是将该持久化路径删除？
			return false;
		}
		// 目前所有用户都有权限在删除默认账户中所共享的内容
		if (file.delete()) {
			// 尝试将持久化内容删除，并删除文件树中的内容
			String fakePath = getFakePath(), realPath = getRealPath();
			getSystem().unpersist(fakePath);
			// 去持久化可能会失败，但是在下次加载系统的时候，应该能够被删除
			getSystem().deleteSharedLink(fakePath);
			return true;
		} else {
			return false;
		}
		// 对于没有写权限的内容，或者对于defaultSp的内容该怎么处理呢？如果没有权限的情况下，需要使用哪个命令来回复呢？
		// 使用等待的方式来触发,将persist的内容删除后就好
	}

	@Override
	public boolean exists() {
		return getRealFile().exists();
	}

	@Override
	public boolean setLastModified(long time) {
		return getRealFile().setLastModified(time);
	}

	@Override
	public boolean renameTo(SharedLink newPath) {
		// 尝试修改真实文件的文件名
		// 检测写权限
		if (!canWrite()) {
			Log.e(TAG, "write permission denied");
			return false;
		}
		// 尝试修改
		File realFile = getRealFile();
		if (realFile == null) {
			// TODO 处理文件不存在的情况
			Log.e(TAG, "file not exist");
			return false;
		}
		// TODO 减少检查
		if (!realFile.isFile()) {
			Log.e(TAG, "is not file");
			return false;
		}
		File toFile = newPath.getRealFile();
		// 尝试重命名
		if (!realFile.renameTo(toFile)) {
			Log.e(TAG, "尝试重命名文件失败");
			return false;
		}
		Log.d(TAG, "真实文件重命名成功:" + realFile.getAbsolutePath());
		
		// 准备内容
		String oldFakePath = getFakePath(), newFakePath = newPath.getFakePath();
		String newRealPath = realFile.getParent() + File.separator + toFile.getName();
		// 需要调整父文件中的内容
		SharedLink parent = getSystem().getSharedLink(getParent());
		parent.list().remove(getName());
		// 更新当前文件对象
		setFakePath(newFakePath);
		setRealPath(newRealPath);
		// 向父文件添加新的内容
		parent.list().put(getName(), this);
		// 尝试修正持久化内容
		getSystem().changePersist(oldFakePath, newFakePath, newRealPath);
		return true;
	}
	
}
