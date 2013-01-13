package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

public enum TextFileType {

	Webcrawl() {
		@Override
		public String getShortForm() {
			return "cr";
		}
	},

	Findlinks() {
		@Override
		public String getShortForm() {
			return "fl";
		}
	};

	private final String outputName;

	private TextFileType() {
		this("web");
	}

	private TextFileType( final String prefix ) {
		final String inputPrefix;
		if (prefix == null) {
			inputPrefix = "";
		} else {
			inputPrefix = prefix;
		}

		final String shortForm = this.getShortForm();
		if (shortForm != null) {
			this.outputName = String.format("%s-%s",inputPrefix, shortForm.toLowerCase());
		} else {
			throw new IllegalArgumentException("The short form of the assigned enum '" + this.toString()
					+ "' has to be initialized.");
		}
	}

	public abstract String getShortForm();

	public String getOutputName() {
		return this.outputName;
	}

	public static TextFileType parse(final String name) throws IllegalArgumentException {
		final String lowercaseName = name.toLowerCase();
		final String shortenedForm = lowercaseName.substring(0, 2);

		for (TextFileType type : TextFileType.values()) {
			if (shortenedForm.equals(type.getShortForm()) || name.contains(type.getOutputName())) { return type; }
		}
		throw new IllegalArgumentException(String.format("Not recognizable %s: '%s'", TextFileType.class.getSimpleName(), name));
	}

}
