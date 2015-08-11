package edu.udel.cis.vsl.gmc;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.udel.cis.vsl.gmc.Option.OptionType;

/**
 * <p>
 * A GMCSection has a unique name and it encapsulates a set of key-value pairs,
 * where the keys correspond to commandline parameters and the value is the
 * value assigned to that parameter. In addition, there can be any number of
 * "free arguments" that are not assigned to any parameter. The free arguments
 * are always strings.
 * </p>
 * 
 * <p>
 * A section is typically generated by parsing a command line, or at least the
 * suffix of a command line after the initial command(s). The command line
 * suffix <code>-verbose -errBound=10 filename.c</code>, for example, would
 * yield an anonymous section in which "verbose" is mapped to true, "errBound"
 * is mapped to the integer 10, and with one free argument "fileName.c". An
 * anonymous section has the name "anonymous" which is a reserved name for
 * anonymous sections.
 * </p>
 * 
 * <p>
 * Each GMCSection has associated to it a configuration, which has it either as
 * the anonymous section or in its section map.
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
 * @author Manchun Zheng (zmanchun)
 * 
 */
public class GMCSection implements Serializable {

	// Instance fields...

	/**
	 * required and generated by eclipse
	 */
	private static final long serialVersionUID = -4500313731497066831L;

	/**
	 * The GMC configuration that this section belongs to.
	 */
	private GMCConfiguration config;

	/**
	 * The name of this section
	 */
	private String name;

	/**
	 * Map from option to value assigned to that option. Only options that have
	 * a non-null value assigned to them will have an entry in this map. Any
	 * option added to this map has to be contained in the configuration
	 * associated with this section.
	 */
	private Map<Option, Object> valueMap = new LinkedHashMap<>();

	/**
	 * The list of free arguments associated to this configuration.
	 */
	private ArrayList<String> freeArgs = new ArrayList<>();

	// Constructors...

	/**
	 * Constructs a new GMCSection with the given name
	 * 
	 * @param name
	 *            the name of the section
	 */
	public GMCSection(String name) {
		this.name = name;
	}

	/**
	 * Constructs a new instance of GMCSection with the given GMCConfiguration
	 * and name.
	 * 
	 * @param config
	 *            The GMCConfiguraiton that the new section associates with.
	 * @param name
	 *            The name of the new section
	 */
	public GMCSection(GMCConfiguration config, String name) {
		this.name = name;
		this.config = config;
	}

	// Public methods...

	/**
	 * Gets the value associated to an option or null if no value is associated
	 * to that option.
	 * 
	 * @param option
	 *            an option associated to this configuration
	 * @return the value assigned to the option or null
	 * @throws IllegalArgumentException
	 *             if the given option is not associated to this configuration
	 */
	public Object getValue(Option option) {
		config.checkContainsOption(option);
		return valueMap.get(option);
	}

	/**
	 * Returns the value associated to an option or the default value for that
	 * option if no value is associated to it.
	 * 
	 * @param option
	 *            an option associated to this configuration
	 * @return the value assigned to the option or the option's default value
	 * @throws IllegalArgumentException
	 *             if the given option is not associated to this configuration
	 */
	public Object getValueOrDefault(Option option) {
		Object value = getValue(option);

		if (value == null)
			return option.defaultValue();
		else
			return value;
	}

	/**
	 * Gets the map value associated to an option of map type, or null if no
	 * value is associated to that option.
	 * 
	 * @param option
	 *            an option of map type controlled by this configuration
	 * @return the map value associated to the option or null
	 * @throws IllegalArgumentException
	 *             if the given option does not have map type or is not
	 *             associated to this configuration
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getMapValue(Option option) {
		config.checkContainsOption(option);
		if (option.type() != OptionType.MAP)
			throw new IllegalArgumentException(
					"Expected option of map type, say type " + option.type()
							+ " in option " + option.name());
		return (Map<String, Object>) valueMap.get(option);
	}

	/**
	 * Given an option of map type, and a key, this returns the value associated
	 * to the key in the map associated to option. If the map associated to this
	 * option is null, this returns null. If there is no entry in the map for
	 * the key, this returns null.
	 * 
	 * @param option
	 *            an option of map type associated to this configuration
	 * @param key
	 *            a string to be used as the key in the map
	 * @return the value associated to the key or null
	 * @thros IllegalArgumentException if the given option does not have map
	 *        type or is not associated to this configuration
	 */
	public Object getMapEntry(Option option, String key) {
		Map<String, Object> map = getMapValue(option);

		if (map == null)
			return null;
		else
			return map.get(key);
	}

	/**
	 * Determines whether the value associated to a boolean option should be
	 * construed as true in most circumstances. Specifically: if there is a
	 * value associated to this option, this method will return that value. If
	 * there is no value associated to this option but the default value for the
	 * option is true, this method will return true, otherwise it will return
	 * false.
	 * 
	 * @param option
	 *            an option of boolean type controlled by this configuration
	 * @return true iff there is a value associated to that option and it is
	 *         true or there is no value associated to the option and the
	 *         option's default value is true
	 * @throws IllegalArgumentException
	 *             if the given option is not associated to this configuration
	 *             or does not have boolean type
	 */
	public boolean isTrue(Option option) {
		config.checkContainsOption(option);
		if (option.type() != OptionType.BOOLEAN)
			throw new IllegalArgumentException(
					"Expected option of boolean type, saw type "
							+ option.type() + " in option " + option.name());
		else {
			Boolean value = (Boolean) getValue(option);

			if (value != null)
				return value;
			value = (Boolean) option.defaultValue();
			return value != null && value;
		}
	}

	/**
	 * Returns the current number of free arguments associated to this
	 * configuration.
	 * 
	 * @return number of free arguments
	 */
	public int getNumFreeArgs() {
		return freeArgs.size();
	}

	/**
	 * Returns the i-th free argument, indexed from 0.
	 * 
	 * @param i
	 *            integer in range 0..n-1, where n is the current number of free
	 *            arguments assigned to this configuration
	 * @return the i-th free argument
	 */
	public String getFreeArg(int i) {
		return freeArgs.get(i);
	}

	/**
	 * Returns the name of this section.
	 * 
	 * @return the name of this section
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Adds a free argument to the list of free arguments associated to this
	 * configuration.
	 * 
	 * @param arg
	 *            a String
	 */
	public void addFreeArg(String arg) {
		freeArgs.add(arg);
	}

	/**
	 * Adds the key-value pair for a scalar value to the parameter map. If an
	 * entry with that key already exists, it is replaced by the new one. If the
	 * value is null, this instead removes the entry with that key (if one
	 * exists), returning the old entry.
	 * 
	 * @param key
	 *            a non-null string, the name of the parameter
	 * @param value
	 *            the value to associate to the parameter; either null or a
	 *            non-null Boolean, Integer, Double, or String
	 * @return the previous value associated to key, or null if there was none
	 * @throws IllegalArgumentException
	 *             if option is not associated to this configuration, or if
	 *             value does not have a type compatible with the type of the
	 *             option, or if the option has MAP type
	 */
	public Object setScalarValue(Option option, Object value) {
		config.checkContainsOption(option);
		if (value == null)
			return valueMap.remove(option);
		switch (option.type()) {
		case BOOLEAN:
			if (value instanceof Boolean)
				return valueMap.put(option, value);
			else
				throw new IllegalArgumentException("Option " + option.name()
						+ ": expected boolean, saw " + value);
		case DOUBLE:
			if (value instanceof Double)
				return valueMap.put(option, value);
			if (value instanceof Float)
				return valueMap.put(option, new Double((Float) value));
			if (value instanceof Integer)
				return valueMap.put(option, new Double((Integer) value));
			else
				throw new IllegalArgumentException("Option " + option.name()
						+ ": expected double, saw " + value);
		case INTEGER:
			if (value instanceof Integer)
				return valueMap.put(option, value);
			else
				throw new IllegalArgumentException("Option " + option.name()
						+ ": expected integer, saw " + value);
		case MAP:
			throw new IllegalArgumentException("Expected scalar value, saw map");
		case STRING:
			if (value instanceof String)
				return valueMap.put(option, value);
			else
				throw new IllegalArgumentException("Option " + option.name()
						+ ": expected string, saw " + value);
		default:
			throw new RuntimeException("unreachable");
		}
	}

	/**
	 * Sets map value or removes map entry from this configuration.
	 * 
	 * @param option
	 *            an option of MAP type associated to this configuration
	 * @param value
	 *            a map to assign to that option, or null
	 * @return the old map value associated to the option, or null if there
	 *         wasn't one
	 * @throws IllegalArgumentException
	 *             if the option is not associated to this configuration, or if
	 *             option's type is not MAP
	 */
	public Map<String, Object> setMapValue(Option option,
			Map<String, Object> value) {
		config.checkContainsOption(option);
		if (value == null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> result = (Map<String, Object>) valueMap
					.remove(option);

			return result;
		}
		if (option.type() != OptionType.MAP)
			throw new IllegalArgumentException(
					"Expected option of map type, saw type " + option.type()
							+ " in option " + option.name());
		else {
			@SuppressWarnings("unchecked")
			Map<String, Object> result = (Map<String, Object>) valueMap.put(
					option, value);

			return result;
		}
	}

	/**
	 * Given an option of map type, adds or removes the key-value pair to the
	 * map corresponding to that option.
	 * 
	 * If the given option does not currently have an assigned value, a new
	 * empty map is created for it.
	 * 
	 * If the key is null, this removes the entry with that key if one exists.
	 * 
	 * @param option
	 *            an option of map type that is associated to this configuration
	 * @param key
	 *            a string which is the key for the map entry
	 * @param value
	 *            a scalar value which is an instance of one of String, Integer,
	 *            Double, or Boolean
	 * @return the previous value associated to that key or null if there was
	 *         none
	 * 
	 * @throws IllegalArgumentException
	 *             if the given option is not associated to this configuration,
	 *             or it does not have map type
	 */
	public Object putMapEntry(Option option, String key, Object value) {
		Map<String, Object> map = getMapValue(option);

		if (map == null) {
			map = new LinkedHashMap<String, Object>();
			valueMap.put(option, map);
		}
		if (value == null)
			return map.remove(key);
		else
			return map.put(key, value);
	}

	/**
	 * Returns a deep copy of this configuration. The two configurations will
	 * share references to the same options, and to the same Strings, but not to
	 * anything else. As options and strings are immutable, this should not be a
	 * problem.
	 * 
	 * @return deep copy of this section
	 */
	@Override
	public GMCSection clone() {
		GMCSection result = new GMCSection(this.name);
		int numArgs = getNumFreeArgs();

		result.config = config;
		for (int i = 0; i < numArgs; i++)
			result.addFreeArg(getFreeArg(i));
		for (Entry<Option, Object> entry : valueMap.entrySet()) {
			Option option = entry.getKey();
			OptionType type = option.type();

			if (type == OptionType.MAP) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) entry
						.getValue();

				for (Entry<String, Object> mapEntry : map.entrySet())
					result.putMapEntry(option, mapEntry.getKey(),
							mapEntry.getValue());
			} else {
				result.setScalarValue(option, entry.getValue());
			}
		}
		return result;
	}

	/**
	 * Modifies this section by reading in the values of the given configuration
	 * and using those to set values of this one. Existing entries in this one
	 * may be overwritten in the process.
	 * 
	 * @param that
	 *            another configuration; the set of options associated to that
	 *            should be a subset of the set of options associated to this
	 */
	public void read(GMCSection that) {
		for (Entry<Option, Object> entry : that.valueMap.entrySet()) {
			Option option = entry.getKey();
			OptionType type = option.type();

			if (type == OptionType.MAP) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) entry
						.getValue();

				for (Entry<String, Object> mapEntry : map.entrySet())
					putMapEntry(option, mapEntry.getKey(), mapEntry.getValue());
			} else {
				setScalarValue(option, entry.getValue());
			}
		}
	}

	/**
	 * Prints the current state of this configuration in a manner similar to
	 * what would appear on a commandline. Each scalar assignment appears on one
	 * line. At the end the free arguments are printed, one on each line. Never
	 * prints the name of anonymous sections.
	 * 
	 * @param out
	 *            print stream to which to print
	 */
	public void print(PrintStream out) {
		if (!this.name.equals(GMCConfiguration.ANONYMOUS_SECTION)) {
			out.print("--");
			out.println(this.name);
		}
		for (Entry<Option, Object> entry : valueMap.entrySet()) {
			Option option = entry.getKey();
			OptionType optionType = option.type();
			String optionName = option.name();

			if (optionType == OptionType.MAP) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) entry
						.getValue();

				for (Entry<String, Object> mapEntry : map.entrySet()) {
					out.print("-" + optionName + mapEntry.getKey());
					out.print("=");
					GMCConfiguration.printValue(out, mapEntry.getValue());
					out.println();
				}
			} else {
				out.print("-" + optionName + "=");
				GMCConfiguration.printValue(out, entry.getValue());
				out.println();
			}
		}
		for (String arg : freeArgs)
			out.println(arg);
		out.flush();
	}

	/**
	 * Updates the configuration associates with this section.
	 * 
	 * @param config
	 *            The configuration that this section belongs to.
	 */
	public void setConfiguration(GMCConfiguration config) {
		this.config = config;
	}

}
