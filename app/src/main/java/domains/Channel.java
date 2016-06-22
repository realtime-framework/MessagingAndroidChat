package domains;

import java.util.ArrayList;
import java.util.List;

public class Channel {
	private final String name;
	private final List<Message> messages;
	private int unRead;
	
	public Channel(String name) {
		this.name = name;
		this.messages = new ArrayList<>();
		this.unRead = 0;
	}
	
	public void addMessage(Message msg)
	{
		this.messages.add(msg);
	}
	
	public List<Message> getMessages(){
		return this.messages;
	}

	public int getUnRead() {
		return unRead;
	}

	public void setUnRead(int unRead) {
		this.unRead = unRead;
	}
	

}
