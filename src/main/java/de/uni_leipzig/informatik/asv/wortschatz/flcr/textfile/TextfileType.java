package de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile;

public enum TextfileType {

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

	private TextfileType() {
		this("web");
	}

	private TextfileType(final String prefix) {
		final String inputPrefix;
		if (prefix == null) {
			inputPrefix = "";
		} else {
			inputPrefix = prefix;
		}

		final String shortForm = this.getShortForm();
		if (shortForm != null) {
			this.outputName = inputPrefix + shortForm.toLowerCase();
		} else {
			throw new IllegalArgumentException("The short form of the assigned enum '" + this.toString()
					+ "' has to be initialized.");
		}
	}

	public abstract String getShortForm();

	public String getOutputName() {
		return this.outputName;
	}

	public static TextfileType parse(final String name) {
		final String lowercaseName = name.toLowerCase();
		final String shortenedForm = lowercaseName.substring(0, 2);

		for (TextfileType type : TextfileType.values()) {
			if (shortenedForm.equals(type.getShortForm()) || name.contains(type.getOutputName())) { return type; }
		}
		return null;
	}

}
