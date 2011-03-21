package com.bb.android.sdk.demo.model;

public class Topic implements Comparable<Topic> {
	
	public Topic(String k) {
		this.keyword = k;
	}
	
	public String keyword;
	public boolean notificationEnabled = false;
	public boolean searchOnlyInTitle = false;

	@Override
	public int compareTo(Topic t) {
		return this.keyword.toLowerCase().compareTo(t.keyword.toLowerCase());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Topic other = (Topic) obj;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		return true;
	} 
	
	@Override
	public String toString() {
		return keyword+"("+notificationEnabled+")";
	}
}
