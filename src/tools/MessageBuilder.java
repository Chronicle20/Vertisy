package tools;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 1, 2016
 */
public class MessageBuilder{

	private String content = "";
	private boolean tts = false;

	public MessageBuilder(){}

	/**
	 * Sets the content of the message.
	 *
	 * @param content The message contents.
	 * @return The message builder instance.
	 */
	public MessageBuilder withContent(String content){
		this.content = "";
		return appendContent(content);
	}

	/**
	 * Sets the content of the message with a given style.
	 *
	 * @param content The message contents.
	 * @param styles The styles to be applied to the content.
	 * @return The message builder instance.
	 */
	public MessageBuilder withContent(String content, Styles... styles){
		this.content = "";
		return appendContent(content, styles);
	}

	/**
	 * Appends extra text to the current content.
	 *
	 * @param content The content to append.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendContent(String content){
		this.content += content;
		return this;
	}

	/**
	 * Appends extra text to the current content with given style.
	 *
	 * @param content The content to append.
	 * @param styles The styles to be applied to the new content.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendContent(String content, Styles... styles){
		for(Styles style : styles)
			this.content += style.getMarkdown();
		this.content += content;
		ArrayUtils.reverse(styles);
		for(Styles style : styles)
			this.content += style.getReverseMarkdown();
		return this;
	}

	/**
	 * Sets the message to have tts enabled.
	 *
	 * @return The message builder instance.
	 */
	public MessageBuilder withTTS(){
		tts = true;
		return this;
	}

	/**
	 * This sets the content to a multiline code block with no language highlighting.
	 *
	 * @param content The content inside the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder withQuote(String content){
		return withCode("", content);
	}

	/**
	 * Adds a multiline code block with no language highlighting.
	 *
	 * @param content The content inside the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendQuote(String content){
		return appendCode("", content);
	}

	/**
	 * Sets the content to a multiline code block with specific language syntax highlighting.
	 *
	 * @param language The language to do syntax highlighting for.
	 * @param content The content of the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder withCode(String language, String content){
		this.content = "";
		return appendCode(language, content);
	}

	/**
	 * Adds a multiline code block with specific language syntax highlighting.
	 *
	 * @param language The language to do syntax highlighting for.
	 * @param content The content of the code block.
	 * @return The message builder instance.
	 */
	public MessageBuilder appendCode(String language, String content){
		return appendContent(language + "\n" + content, Styles.CODE_WITH_LANG);
	}

	/**
	 * This gets the content of the message in its current form.
	 *
	 * @return The current content of the message.
	 */
	public String getContent(){
		return content;
	}

	/**
	 * Enum describing Markdown formatting that can be used in chat.
	 */
	public enum Styles{
		ITALICS("*"),
		BOLD("**"),
		BOLD_ITALICS("***"),
		STRIKEOUT("~~"),
		CODE("``` "),
		INLINE_CODE("`"),
		UNDERLINE("__"),
		UNDERLINE_ITALICS("__*"),
		UNDERLINE_BOLD("__**"),
		UNDERLINE_BOLD_ITALICS("__***"),
		CODE_WITH_LANG("```");

		final String markdown, reverseMarkdown;

		Styles(String markdown){
			this.markdown = markdown;
			this.reverseMarkdown = new StringBuilder(markdown).reverse().toString();
		}

		/**
		 * Gets the markdown formatting for the style.
		 *
		 * @return The markdown formatting.
		 */
		public String getMarkdown(){
			return markdown;
		}

		/**
		 * Reverses the markdown formatting to be appended to the end of a formatted string.
		 *
		 * @return The reversed markdown formatting.
		 */
		public String getReverseMarkdown(){
			return reverseMarkdown;
		}

		@Override
		public String toString(){
			return markdown;
		}
	}
}