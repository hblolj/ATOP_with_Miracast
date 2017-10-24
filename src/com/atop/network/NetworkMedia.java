package com.atop.network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetworkMedia extends Thread {
	private final String TAG = "Class::NetworkMedia";

	private Socket MediaSocket;

	private String server_ip;
	private static final int server_port = 2000;
	private DataInputStream fileIm;

	private String dirPath = "/sdcard/ATOP/Stream/"; // ������ ���� �� ���
	
	private FileOutputStream fos;
	private BufferedOutputStream bos;
	private boolean socketConnect = false;

	private byte[] fileNameByte; // ���� �̸� �޾ƿ��� ����Ʈ
	private int fileNameSize; // ���� �̸��� ����Ʈũ��
	private String fileName;
	private long fileAllSize; // ���� ��ü ũ��
	private String FILEBACK = "";
	private Handler mhandler;
	private static int TempCount = 0; // �� �޾Ҵ���
	private boolean isinit = false;

	public NetworkMedia(String ip, Context context, Handler handler) {
		this.server_ip = ip;
		this.mhandler = handler;
	}

	public boolean istcpConnet() {
		return socketConnect;
	}

	public void makedir(){
		File dirfile = new File(dirPath);
		if (!dirfile.exists()) { // ���������
			dirfile.mkdirs();
		}
	}
	
	public void run() {

		try {
			MediaSocket = new Socket();
			InetSocketAddress socketAddr = new InetSocketAddress(server_ip,
					server_port);
			MediaSocket.connect(socketAddr, 5000);

			Log.e(TAG, "file new socket ");
			socketConnect = true;
			sendMessage("Open"); // ������ �����°� �������� �˸�
			TempCount = 0;
			while (socketConnect) {
				makedir();
				isinit = true;
				fileIm = null;
				fileIm = new DataInputStream(MediaSocket.getInputStream());

				fileNameByte = new byte[1024];
				fileNameSize = fileIm.read(fileNameByte); // ���� �̸��� ����.
				fileName = new String(fileNameByte, 0, fileNameSize, "UTF-8");
				String[] subfileName = fileName.split("\\."); // Ȯ����Ȯ��

				fileAllSize = Long.parseLong(subfileName[0]);
				// FILEBACK = "."+subfileName[2]; // ��ģ���� Ȯ���� ģ����
				FILEBACK = ".mp4"; // mp4 �� ����
				String time = subfileName[3];

				Bundle b = new Bundle();
				b.putInt("flag", 1);
				b.putLong("filelen", fileAllSize);
				b.putString("fileback", FILEBACK);
				b.putString("alltime", time);
				Message msg = mhandler.obtainMessage();
				msg.setData(b);
				mhandler.sendMessage(msg);

				while (TempCount < 20 && isinit) {
					sendMessage("Okay");
					sendTemp_File();
				}

				sendMessage("Open");
			}
		} catch (IOException e) {
			socketConnect = false;
			Log.e(TAG, "Socket Connect Exception2 : " + e); // ���� IoException

		} catch (Exception e) { // ���� Exception
			socketConnect = false;
			Log.e(TAG, "Exception : " + e);
		} finally {
			try {

				socketConnect = false;
				MediaSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "socket close : " + e);
			}
		}
	} // end of run

	public void sendTemp_File() {
		try {
			fileNameByte = new byte[2048];

			fileNameSize = fileIm.read(fileNameByte); // ���� �̸��� ����.

			fileName = new String(fileNameByte, 0, fileNameSize, "UTF-8");

			String[] subfile = fileName.split("/"); // Ȯ����Ȯ��

			int tempsize = Integer.parseInt(subfile[0]);
			fileName = subfile[1];

			File receiveFile = new File(dirPath + fileName); // ��¥ ����
			fos = new FileOutputStream(receiveFile);
			bos = new BufferedOutputStream(fos); // ���� ����

			sendMessage("Ready"); // ���� ���� �غ� ������ �˸�.

			long len = 0;
			int size = 7168;
			byte[] data = new byte[size];
			while (len != tempsize && socketConnect) { // len�� size���� �۰� seek�ٸ�
														// �ȿ���������
				int readSize = fileIm.read(data);
				len = len + readSize;
				bos.write(data, 0, readSize);
				bos.flush();
				// Log.e(TAG,"len : "+ len + " �޾ƾ��� ������ : " + tempsize);
			}
			bos.close();
			fos.close(); // ���ϴݱ�

			if (isinit == true) {

				TempCount++; // ������ ������ �ϳ��� ������

				String[] str = fileName.split("\\.");
				String num = str[0].substring(4, str[0].length());

				Message msgg = mhandler.obtainMessage();
				Bundle bb = new Bundle();
				bb.putInt("flag", 3);
				bb.putInt("savefile", Integer.parseInt(num));
				msgg.setData(bb);
				mhandler.sendMessage(msgg);
				Log.e(TAG, "�Լ� ������ : " + TempCount);
			}
		} catch (Exception e) {
			Log.e(TAG, "err : " + e);
		}
	}

	public void sendMessage(String message) {
		try {
			MediaSocket.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			// Log.e(TAG, "��Ŷ ���� ����." + e);

		}
	}

	public void setinit() {
		TempCount = 0;
		isinit = false;
	}

	public void Close_Socket() {
		try {
			TempCount = 100;
			socketConnect = false;
			MediaSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "Socket Close Failed");
			e.printStackTrace();
		}
	}

}