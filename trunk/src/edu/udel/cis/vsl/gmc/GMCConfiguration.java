package edu.udel.cis.vsl.gmc;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * A GMCConfiguration is composed of a number of GMCSection's, each of which
 * encapsulates a set of key-value pairs, where the keys correspond to
 * commandline parameters and the value is the value assigned to that parameter.
 * In addition, there can be any number of "free arguments" that are not
 * assigned to any parameter. The free arguments are always strings.
 * </p>
 * 
 * <p>
 * A configuration is typically generated by parsing a command line, or at least
 * the suffix of a command line after the initial command(s). The command line
 * suffix <code>-verbose -errBound=10 filename.c</code>, for example, would
 * yield a configuration with an anonymous GMCSection in which "verbose" is
 * mapped to true, "errBound" is mapped to the integer 10, and with one free
 * argument "fileName.c".
 * </p>
 * 
 * <p>
 * A configuration has associated to it a set of options, which are specified
 * when the configuration is instantiated. And each GMCSection has associated to
 * it a configuration.
 * </p>
 * 
 * <p>
 * Each option is an instance of class {@link Option}. An option has a name and
 * a type. The type is one of STRING, INTEGER, DOUBLE, BOOLEAN, or MAP. The type
 * determines the kind of value that can be assigned to an option. The first
 * four are "scalar" types and take values of type String, Integer, Double, and
 * Boolean, respectively.
 * </p>
 * 
 * <p>
 * An option of MAP type may be assigned a value of type Map<String,Object>.
 * This kind of option is provided for command line arguments such as
 * <code>-inputX=10 -inputB=true -inputZ="hello"</code>. This constructs a map
 * from String to Object which maps "X" to the Integer 10, "B" to the Boolean
 * true, and "Z" to the String "hello". This map is the value that is assigned
 * to the option named "input". In general, the values of the map can be any
 * scalar values, and their types will be inferred from their format. For
 * example 1.0 will be interpreted as a Double, 1 as an Integer, "1" (with
 * quotes) a String.
 * </p>
 * 
 * 
 * @author Stephen F. Siegel
 * 
 */
public class GMCConfiguration implements Serializable{

	// Instance fields...

	/**
	 * The reserved name of the anonymous GMCSection of a configuration.
	 */
	public static final String ANONYMOUS_SECTION = "anonymous";

	/**
	 * The anonymous GMCSection of this configuration. Could be null.
	 */
	private GMCSection anonymousSection;

	/**
	 * Map from option name to option, for all options associated to this
	 * configuration.
	 */
	private Map<String, Option> optionMap = new LinkedHashMap<>();

	/**
	 * Map from section name to section, for all non-anonymous sections
	 * associated to this configuratsion.
	 */
	private Map<String, GMCSection> sectionMap = new LinkedHashMap<>();

	// Constructors...

	/**
	 * Constructs new configuration with the set of options obtained from the
	 * given collection. The set of options associated to this configuration is
	 * determined from the given collection. Duplicates in the collection will
	 * be ignored. The new configuration will be empty: i.e., it will have no
	 * entries, i.e., nothing will be assigned to any option.
	 * 
	 * @param options
	 *            a collection of non-null options; duplicates will be ignored.
	 *            The options must have distinct names
	 * @throws IllegalArgumentException
	 *             if two options in the collection have the same name
	 */
	public GMCConfiguration(Collection<Option> options) {
		for (Option option : options) {
			if (optionMap.put(option.name(), option) != null)
				throw new IllegalArgumentException("Saw two options named "
						+ option.name());
		}
		anonymousSection = new GMCSection(this, ANONYMOUS_SECTION);
	}

	// Helper methods...

	/**
	 * Checks that the given option is associated to this configuration.
	 * 
	 * @param option
	 *            an Option
	 * @throws IllegalArgumentException
	 *             if the given option is not associated to this configuration
	 */
	void checkContainsOption(Option option) {
		Option actual = optionMap.get(option.name());

		//if (actual == null || !actual.equals(option))
		//	throw new IllegalArgumentException("Option " + option.name()
		//			+ " is not associated to this configuration");
	}

	/**
	 * Processes escape characters of the given string.
	 * 
	 * @param string
	 *            The string whose escape characters are to be processed.
	 * @return A string which is the result of processing the escape characters
	 *         of the given string.
	 */
	private static String escapeString(String string) {
		String result = string;

		result = result.replace("\\", "\\" + "\\");
		result = result.replace("\n", "\\" + "n");
		result = result.replace("\t", "\\" + "t");
		result = result.replace("\"", "\\" + "\"");
		result = "\"" + result + "\"";
		return result;
	}

	/**
	 * Prints the value of an option. Needs to handle escape characters for
	 * strings.
	 * 
	 * @param out
	 *            The print stream to be used.
	 * @param value
	 *            The value to be printed.
	 */
	static void printValue(PrintStream out, Object value) {
		if (value instanceof String) {
			out.print(escapeString((String) value));
		} else
			out.print(value);
	}

	// Public methods...

	/**
	 * Returns the set of options associated to this configuration. This returns
	 * all options associated to this configuration, not just those that have a
	 * value assigned to them.
	 * 
	 * @return the set of options
	 */
	public Collection<Option> getOptions() {
		return optionMap.values();
	}

	/**
	 * Returns the option with the given name associated to this configuration,
	 * or null if there is none.
	 * 
	 * @param name
	 *            the name of an option
	 * @return the option with that name
	 */
	public Option getOption(String name) {
		return optionMap.get(name);
	}

	/**
	 * Returns the section with the given name, or null if there is none.
	 * 
	 * @param name
	 *            the name of the section
	 * @return the section with that name
	 */
	public GMCSection getSection(String name) {
		if (this.anonymousSection != null
				&& this.anonymousSection.getName().equals(name))
			return this.anonymousSection;
		return this.sectionMap.get(name);
	}

	/**
	 * Returns the anonymous section
	 * 
	 * @return the anonymous sections
	 */
	public GMCSection getAnonymousSection() {
		return this.anonymousSection;
	}

	/**
	 * Returns the number of NON-anonymous sections.
	 * 
	 * @return the number of NON-anonymous sections.
	 */
	public int getNumSections() {
		return this.sectionMap.size();
	}

	/**
	 * Updates the anonymous section with the given section. Also updates the
	 * configuration associates with the given section to be this configuration.
	 * 
	 * @param section
	 *            The section to be used as the anonymous section of this
	 *            configuration.
	 */
	public void setAnonymousSection(GMCSection section) {
		this.anonymousSection = section;
		this.anonymousSection.setConfiguration(this);
	}

	/**
	 * Adds a section to the NON-anonymous section map. Also updates the
	 * configuration associates with the given section to be this configuration.<br>
	 * Precondition: <code>!section.getName().equals(DEFAULT_SECTION)</code>.
	 * 
	 * @param section
	 *            The section to be added
	 */
	public void addSection(GMCSection section) {
		assert !section.getName().equals(ANONYMOUS_SECTION);
		section.setConfiguration(this);
		this.sectionMap.put(section.getName(), section);
	}

	/**
	 * Returns a deep copy of this configuration. The two configurations will
	 * share references to the same options, and to the same Strings, but not to
	 * anything else. As options and strings are immutable, this should not be a
	 * problem.
	 * 
	 * @return deep copy of this configuration
	 */
	@Override
	public GMCConfiguration clone() {
		GMCConfiguration result = new GMCConfiguration(getOptions());

		if (this.anonymousSection != null)
			result.setAnonymousSection(anonymousSection.clone());
		for (GMCSection section : sectionMap.values()) {
			result.addSection(section.clone());
		}
		return result;
	}

	/**
	 * Returns an iterable object of the NON-anonymous sections.
	 * 
	 * @return the iterable object of the NON-anonymous sections.
	 */
	public Iterable<GMCSection> getSections() {
		return this.sectionMap.values();
	}

	/**
	 * Modifies this configuration by reading in the values of the given
	 * configuration and using those to set values of this one. Existing entries
	 * in this one may be overwritten in the process.
	 * 
	 * @param that
	 *            another configuration; the set of options associated to that
	 *            should be a subset of the set of options associated to this
	 */
	public void read(GMCConfiguration that) {
		if (that.anonymousSection != null)
			this.anonymousSection = that.anonymousSection.clone();
		else
			this.anonymousSection = null;
		for (GMCSection thatSection : that.getSections()) {
			String name = thatSection.getName();

			if (this.sectionMap.containsKey(name))
				this.sectionMap.get(name).read(thatSection);
			else
				this.sectionMap.put(name, thatSection.clone());
		}
	}

	/**
	 * Prints the current state of this configuration in a manner similar to
	 * what would appear on a commandline. Each scalar assignment appears on one
	 * line. At the end the free arguments are printed, one on each line.
	 * 
	 * @param out
	 *            print stream to which to print
	 */
	public void print(PrintStream out) {
		if (this.anonymousSection != null)
			anonymousSection.print(out);
		for (GMCSection section : this.getSections())
			section.print(out);
		out.flush();
	}
}
