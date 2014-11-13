package parser;

public abstract class Meta {
	public int count;
	public String url;

	public Meta() {}

	public Meta(String url) {
		this.url = url;
	}

	public Meta(String url, int count) {
		this.url = url;
		this.count = count;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}