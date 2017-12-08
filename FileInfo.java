
public class FileInfo implements Comparable<FileInfo>{
	String Name;
	String fileName;
	double rating;
	int ratingNums;
	
	public FileInfo(String name, String filename) {
		this.Name = name;
		this.fileName = filename;
		this.rating = 5.0;
		this.ratingNums = 0;
	}
	
	public String toString() {
		return Name + " " + rating + " " + fileName;
	}

	@Override
	public int compareTo(FileInfo o) {
		return (int) Math.signum(this.rating - o.rating);
	}
}
