package org.nectarframework.base.service.template;

import java.util.LinkedList;

import org.apache.commons.lang3.StringEscapeUtils;

public class CompiledTemplate {

	public abstract class Chunk {
		public abstract String toJavaCode();
	}
	
	public class TextChunk extends Chunk {
		String s;
		
		public TextChunk(String string) {
			s = string;
		}

		public String toJavaCode() {
			return "		os.write(\""+StringEscapeUtils.escapeJava(s)+"\".getBytes());\n";
		}
	}
	
	public class JavaChunk extends Chunk {
		String s;

		public JavaChunk(String string) {
			s = string;
		}

		public String toJavaCode() {
			return "		"+s+"\n";
		}
	}
	
	LinkedList<Chunk> chunkList = new LinkedList<Chunk>();
	
	public StringBuffer getJavaCode() {
		StringBuffer sb = new StringBuffer();
		for (Chunk c : chunkList) {
			sb.append(c.toJavaCode());
		}
		return sb;
	}

	public void addText(String string) {
		if (string.length() > 0) {
			if (!chunkList.isEmpty() && chunkList.getLast() instanceof TextChunk) {
				((TextChunk)chunkList.getLast()).s += string;
			} else {
				chunkList.add(new TextChunk(string));
			}
		}
	}
	
	public void addJava(String string) {
		chunkList.add(new JavaChunk(string));

	}
}
