package pseudocode_compiler;

/**
 * Represents a variable record in the symbol table.
 * Stores the variable's data type and its current runtime value.
 */
public class VariableRecord {
    private final String dataType;  // e.g., "INTEGER", "REAL", "STRING", "BOOLEAN"
    private Object value;           // The current value of the variable

    /**
     * Constructs a VariableRecord with a data type and initial value.
     *
     * @param dataType the data type of the variable
     * @param value    the initial value of the variable
     */
    public VariableRecord(String dataType, Object value) {
        this.dataType = dataType;
        this.value = value;
    }

    /**
     * Gets the data type of this variable.
     *
     * @return the data type as a String
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Gets the current value of this variable.
     *
     * @return the current value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this variable.
     *
     * @param value the new value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableRecord{" +
                "dataType='" + dataType + '\'' +
                ", value=" + value +
                '}';
    }
}
