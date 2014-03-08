package com.example.RSS;

import android.os.Parcel;
import android.os.Parcelable;

public class Node implements Parcelable {

	private String title;
	private String description;
	private String date;

	Node() {
	}

	public int describeContents() {
		return 0;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void writeToParcel(Parcel parcel, int flags) {
		;
		parcel.writeString(title);
		parcel.writeString(description);
		parcel.writeString(date);
	}

	public static final Creator<Node> CREATOR = new Creator<Node>() {
		public Node createFromParcel(Parcel in) {
			return new Node(in);
		}

		public Node[] newArray(int size) {
			return new Node[size];
		}
	};

	public void setDate(String date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getDate() {
		return date;
	}

	private Node(Parcel parcel) {
		this.title = parcel.readString();
		this.description = parcel.readString();
		this.date = parcel.readString();
	}
}
