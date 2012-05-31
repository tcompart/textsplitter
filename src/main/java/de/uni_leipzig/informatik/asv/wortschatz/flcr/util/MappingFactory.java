package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import java.io.File;

import de.uni_leipzig.asv.clarin.common.tuple.Pair;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Location;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Textfile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextfileType;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl.LocationFilter;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl.SourceFilter;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.util.impl.TextfileLanguageFilter;

public class MappingFactory {

	private static final String DIVISION_SIGN = "_";

	private final Configurator configurator;

	private static final Filter<String> sourceDomainFilter = new SourceFilter();

	private static Filter<Pair<Textfile, String>> textfileLanguageFilter = null;

	private static final Filter<Location> locationFilter = new LocationFilter();

	public MappingFactory() {
		this.configurator = Configurator.getConfiguration();
	}

	public MappingFactory(Configurator inputConfigurator) {
		this.configurator = inputConfigurator;
	}

	Configurator getConfigurator() {
		return this.configurator;
	}

	private String getDefaultFileName(Textfile textfile) {
		String fileName = textfile.getLanguage() + DIVISION_SIGN + textfile.getOutputType().getOutputName();

		if (getConfigurator() != null && getConfigurator().includeYear()) {
			final Integer year = textfile.getYear() != Textfile.DEFAULT_YEAR ? textfile.getYear() : getConfigurator()
					.getYear();
			fileName += DIVISION_SIGN + year;
		}
		return fileName;
	}

	private String getDefaultFileExtension() {
		return getConfigurator().getDefaultFileExtension();
	}

	public File getDefaultTextfileMapping(Textfile textfile) {
		return new File(this.geDefaultParentDirectories(textfile), this.getDefaultFileName(textfile)
				+ this.getDefaultFileExtension());
	}

	public File geDefaultParentDirectories(Textfile textfile) {
		return new File(this.getDefaultOutputDirectory(textfile.getOutputType()), this.getDefaultFileName(textfile));

	}

	public File getDefaultOutputDirectory(TextfileType outputType) {

		if (outputType == null) { throw new NullPointerException(String.format(
				"An initiliazed value enum '%s' was expected.", TextfileType.class.getName())); }
		// this is though to be : findlinks or webcrawl....
		String outputDirectoryName = outputType.toString();

		if (this.configurator != null && this.configurator.getBaseOutputDirectory() != null) { return new File(
				this.configurator.getBaseOutputDirectory(), outputDirectoryName); }

		return new File(outputDirectoryName);
	}

	public boolean isSupportedSourceLanguage(final Textfile inputTextfile, final Source sourceInput) {
		if (textfileLanguageFilter == null) {
			textfileLanguageFilter = new TextfileLanguageFilter(this.getConfigurator());
		}

		if (inputTextfile == null || sourceInput == null) { throw new NullPointerException(); }

		return textfileLanguageFilter.apply(Pair.create(inputTextfile, sourceInput.getLocation().getDomain()));
	}

	public File getSourceDomainMapping(final Textfile textfile, final Source source) {

		if (!this.isSupportedSourceLanguage(textfile, source)) { return this.getDefaultTextfileMapping(textfile); }

		return new File(this.geDefaultParentDirectories(textfile), this.getDefaultFileName(textfile) + DIVISION_SIGN
				+ source.getLocation().getDomain() + this.getDefaultFileExtension());

	}
}
