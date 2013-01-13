package de.uni_leipzig.informatik.asv.wortschatz.flcr.util;

import com.google.common.base.Predicate;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.Source;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFile;
import de.uni_leipzig.informatik.asv.wortschatz.flcr.textfile.TextFileType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingFactory {

	// deu_limited_webfl_2012_0000.txt
	// deu_limited_webfl.txt
	//
	public static final Pattern PATTERN = Pattern.compile( "(([a-zA-Z]{3}(_[a-zA-Z0-9]+)*)(_web(fl|cr)))(_\\d{4}(_\\d{4})?)?\\.txt" );

	@NotNull
	public static final String DEFAULT_DIVISION_SIGN = "_";


	private static final Logger log = LoggerFactory.getLogger( MappingFactory.class );

	@NotNull
	private String divisionSign = DEFAULT_DIVISION_SIGN;

	@NotNull
	private final Configurator configurator;

	private Predicate<Pair<String, String>> textfileLanguageFilter = null;

	public MappingFactory() {
		this.configurator = Configurator.getGlobalConfiguration();
	}

	public MappingFactory( @NotNull final Configurator inputConfigurator ) {
		this.configurator = inputConfigurator;
	}

	@NotNull
	public Configurator getConfigurator() {
		return this.configurator;
	}

	public void setDivisionSign( @NotNull final String inputDivisionSign ) {
		divisionSign = inputDivisionSign;
	}

	private String getDefaultFileName( @NotNull final TextFile textFile ) {
		return textFile.getLanguage() + divisionSign + this.getTextFileType( textFile );
	}

	public String getTextFileType( final TextFile textFile ) {
		return textFile.getOutputType().getOutputName();
	}

	public String getYear( final TextFile textFile ) {
		if ( getConfigurator() != null && getConfigurator().includeYear() ) {
			final Integer year = ( textFile.getYear() != TextFile.DEFAULT_YEAR ? textFile.getYear() : getConfigurator().getYear() );
			return String.format( "%s%d", divisionSign, year );
		}
		return "";
	}

	public String getDefaultFileExtension() {

		final String fileExtension = getConfigurator().getDefaultFileExtension();

		if ( fileExtension.startsWith( "." ) ) {
			return fileExtension;
		}
		return String.format( ".%s", fileExtension );
	}

	public File geDefaultParentDirectories( TextFile textFile ) {
		return new File( this.getDefaultOutputDirectory( textFile.getOutputType() ),
							   this.getDefaultFileName( textFile ) );
	}

	public File getDefaultOutputDirectory( @NotNull TextFileType outputType ) {
		// this should to be : findlinks or webcrawl.... (some other long and verbose name)
		final String outputDirectoryName = outputType.toString();

		final String baseOutputDirectory = this.configurator.getBaseOutputDirectory();
		if ( baseOutputDirectory != null ) {
			return new File( baseOutputDirectory, outputDirectoryName );
		}
		return new File( outputDirectoryName );
	}

	public Predicate<Pair<String, String>> getLanguageFilter() {
		if ( textfileLanguageFilter == null ) {
			textfileLanguageFilter = new TextFileLanguageFilter( this.getConfigurator() );
		}
		return textfileLanguageFilter;
	}

	public void setLanguageFilter( final Predicate<Pair<String, String>> inputLanguageFilter ) {
		this.textfileLanguageFilter = inputLanguageFilter;
	}

	public boolean isSupportedSourceLanguage( @NotNull final TextFile inputTextFile, @NotNull final Source inputSource ) {
		final boolean booleanResult = inputSource.getLocation() != null && this.getLanguageFilter().apply( new Pair<String, String>( inputTextFile.getLanguage(), inputSource.getLocation().getDomain() ) );
		log.info( String.format( "%s '%s' with %s '%s' %s supported by language filter '%s' ", TextFile.class.getSimpleName(), inputTextFile, Source.class.getSimpleName(), inputSource, ( booleanResult ? "is" : "is not" ), this.getLanguageFilter() ) );
		return booleanResult;
	}

	public File getDefaultTextfileMapping( TextFile textFile ) {
		return this.newFile( textFile, new File( this.geDefaultParentDirectories( textFile ), this.getDefaultFileName( textFile ) + this.getYear( textFile ) + this.getDefaultFileExtension()
		), this.getConfigurator().getDefaultSplitSize() );
	}

	public File getSourceDomainMapping( final TextFile textFile, final Source source ) {
		final long time = System.nanoTime();
		final File resultFile;
		if ( !this.isSupportedSourceLanguage( textFile, source ) ) {
			log.debug( "TextFile '{}' (lanuage: {}) with source '{}' are not qualified for a source domain mapping. Mapping the source the default output.", new Object[]{textFile.getTextFileName(), textFile.getLanguage(), source.toString()} );
			resultFile = this.getDefaultTextfileMapping( textFile );
		} else {
			log.debug( "Mapping TextFile '{}' (language: {}) with source '{}' to default source domain output, depending on domain '{}'", new Object[]{textFile.getTextFileName(), textFile.getLanguage(), source, source.getLocation().getDomain()} );
			resultFile = this.newFile( textFile, new File( this.geDefaultParentDirectories( textFile ), textFile.getLanguage() + divisionSign + source.getLocation().getDomain() + divisionSign + this.getTextFileType( textFile ) + this.getYear( textFile ) + this.getDefaultFileExtension() ), this.getConfigurator().getDefaultSplitSize() );
		}
		log.debug("Required: {} ms for finding source domain mapping for textFile '{}' and its source '{}': '{}'", new Object[]{System.nanoTime() - time, textFile, source, resultFile.getName() });
		return resultFile;

	}

	public File newFile( final TextFile textFile, final File inputFile, final long fileSplitSize ) {

		if ( fileSplitSize == Configurator.DEFAULT_SPLIT_SIZE ) {
			return inputFile;
		}

		final String fileName = inputFile.getName();
		final Pattern fileNamePrefixPattern = Pattern.compile( "(([a-zA-Z_-]+)(\\d{4})?(_(\\d{4}))?)" );

		Matcher matcher = fileNamePrefixPattern.matcher( fileName );

		String fileNamePrefix;
		final int numberToIncrement;
		final String fileNameSuffix = this.getDefaultFileExtension();

		final String year;
		if ( this.getYear( textFile ).isEmpty() ) {
			year = null;
		} else if ( this.getYear( textFile ).startsWith( divisionSign ) ) {
			year = this.getYear( textFile ).substring( 1 );
		} else {
			year = this.getYear( textFile );
		}

		if ( matcher.find() ) {
			fileNamePrefix = matcher.group( 2 );

			// no number exists (no year, no number to increment)
			if ( matcher.group( 3 ) == null || matcher.group( 3 ).isEmpty() ) {
				numberToIncrement = 0;
				// if the first number found, is equals the year
				// that is really ugly... what can be done?! the getYear function returns a '_YEAR' string or is empty ''.
			} else if ( year != null && matcher.group( 3 ).equals( year ) ) {
				// then append the year, and DIVISION SIGN
				fileNamePrefix += matcher.group( 3 ) + divisionSign;

				// first one, the second number exists also: increment the second number
				if ( matcher.group( 5 ) != null && !matcher.group( 5 ).isEmpty() ) {
					numberToIncrement = Integer.parseInt( matcher.group( 5 ) );
					// second: no second number exists, therefore initialize a second number by 0
				} else {
					numberToIncrement = 0;
				}
				// last and actually also the least: the found number is not the year: therefore increment by 1
				// TODO probably BUG AREA NR 1: because, how do we know the year? what about, old files like 2011, which should be addressed now 2012...
			} else {
				numberToIncrement = Integer.parseInt( matcher.group( 3 ) );
			}
		} else {
			log.error( "The file name prefix PATTERN did not find any group of input file name: {}. Returning the assigned input file as it is, without any changes.", inputFile.getName() );
			return inputFile;
		}

		// build everything together
		final String splitNumberString;
		if ( inputFile.length() >= fileSplitSize * 1024 ) {
			splitNumberString = String.format( "%04d", numberToIncrement + 1 );
		} else {
			splitNumberString = String.format( "%04d", numberToIncrement );
		}

		File newFile = new File( inputFile.getParent(), fileNamePrefix + splitNumberString + fileNameSuffix );

		// if the expected file is also already full... try recursivly...
		if ( newFile.exists() && newFile.length() >= fileSplitSize * 1024 ) {
			return this.newFile( textFile, newFile, fileSplitSize );
		}
		return newFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( divisionSign.hashCode() );
		result = prime * result + ( configurator.hashCode() );
		result = prime
						 * result
						 + ( ( textfileLanguageFilter == null ) ? 0
									 : textfileLanguageFilter.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		MappingFactory other = ( MappingFactory ) obj;
		if ( !divisionSign.equals( other.divisionSign ) )
			return false;
		if ( !configurator.equals( other.configurator ) )
			return false;
		if ( textfileLanguageFilter == null ) {
			if ( other.textfileLanguageFilter != null )
				return false;
		} else if ( !textfileLanguageFilter.equals( other.textfileLanguageFilter ) )
			return false;
		return true;
	}


}
