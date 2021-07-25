package dna.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import dna.Dna;
import dna.dataStructures.AttributeVector;

@SuppressWarnings("serial")
public class AttributeTableModel extends AbstractTableModel {
	
	public int getColumnCount() {
		return 5;
	}
	
	public int getRowCount() {
		return Dna.data.getAttributes().size();
	}
	
	public String getColumnName(int column) {
		switch( column ){
			case 0: return "Value";
			case 1: return "Color";
			case 2: return "Type";
			case 3: return "Alias";
			case 4: return "Notes";
			default: return null;
		}
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch( columnIndex ){
			case 0: return Dna.data.getAttributes().get(rowIndex).getValue(); 
			case 1: return Dna.data.getAttributes().get(rowIndex).getColor();
			case 2: return Dna.data.getAttributes().get(rowIndex).getType();
			case 3: return Dna.data.getAttributes().get(rowIndex).getAlias();
			case 4: return Dna.data.getAttributes().get(rowIndex).getNotes();
			default: return null;
		}
	}
	
	public Class<?> getColumnClass(int columnIndex) {
		switch( columnIndex ){
			case 0: return String.class;
			case 1: return Color.class;
			case 2: return String.class;
			case 3: return String.class;
			case 4: return String.class;
			default: return null;
		}	
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			Dna.dna.updateAttributeValue(rowIndex, (String) aValue);
		} else if (columnIndex == 1) {
			Dna.dna.updateAttributeColor(rowIndex, (Color) aValue);
		} else if (columnIndex == 2) {
			Dna.dna.updateAttributeType(rowIndex, (String) aValue);
		} else if (columnIndex == 3) {
			Dna.dna.updateAttributeAlias(rowIndex, (String) aValue);
		} else if (columnIndex == 4) {
			Dna.dna.updateAttributeNotes(rowIndex, (String) aValue);
		}
		this.fireTableDataChanged();
	}
	
	/**
	 * Add a row to the table (and its contents to the data structure).
	 * 
	 * @param av  New attribute vector
	 */
	public void addRow(AttributeVector av) {
		Dna.dna.addAttributeVector(av);
		Collections.sort(Dna.data.attributes);
		fireTableDataChanged();
	}
	
	/**
	 * Delete a row from the table.
	 * 
	 * @param row  Index of the attribute vector in the table and data structure
	 */
	public void deleteRow(int row) {
		Dna.dna.deleteAttributeVector(row);
		Collections.sort(Dna.data.attributes);
		fireTableDataChanged();
	}
	
	/**
	 * Sort the table.
	 */
	public void sort() {
		Collections.sort(Dna.data.attributes);
		fireTableDataChanged();
	}
	
	public void clear() {
		Dna.data.attributes = new ArrayList<AttributeVector>();
		fireTableDataChanged();
	}
	
	/**
	 * Get the attribute vector stored in a particular row.
	 * 
	 * @param row  The row that should be retrieved
	 * @return     An AttributeVector object
	 */
	public AttributeVector get(int row) {
		return Dna.data.getAttributes().get(row);
	}
}