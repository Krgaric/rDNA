package guiCoder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

import dna.Dna;
import dna.Document;
import dna.Statement;

@SuppressWarnings("serial")
public class DocumentEditor extends JDialog {
	
	String dbfile;
	SpinnerDateModel dateModel;
	JSpinner dateSpinner;
	JScrollPane textScroller;
	JButton okButton;
	JPanel newArticlePanel;
	JXTextField titleField;
	JXTextArea textArea, notesArea;
	JXComboBox authorBox, sourceBox, sectionBox, typeBox;
	int[] documentIds;
	ArrayList<Document> documents;

	public ArrayList<Document> getDocuments() {
		return documents;
	}
	
	public DocumentEditor(int[] documentIds) {
		this.documentIds = documentIds;
		documents = Dna.sql.getDocuments(documentIds);
		createGui(documentIds.length);
	}

	public DocumentEditor() {
		createGui(0);
	}

	private void createGui(int numDocuments) {
		this.setModal(true);
		if (numDocuments == 0) {
			this.setTitle("Add new document...");
		} else {
			String s = " document...";
			if (numDocuments > 1) {
				s = " documents...";
			}
			this.setTitle("Edit " + numDocuments + s);
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageIcon tableAddIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-file-plus.png"));
		this.setIconImage(tableAddIcon.getImage());
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		textArea = new JXTextArea("paste the contents of the document here using Ctrl-V...");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(700, 500));
		textScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// first column: labels
		gbc.insets = new Insets(3, 0, 3, 7);
		gbc.gridy = 0;
		gbc.gridx = 0;
		JLabel titleLabel = new JLabel("title", JLabel.RIGHT);
		fieldsPanel.add(titleLabel, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		JLabel dateLabel = new JLabel("date", JLabel.RIGHT);
		fieldsPanel.add(dateLabel, gbc);

		gbc.gridy = 2;
		gbc.gridx = 0;
		JLabel coderLabel = new JLabel("coder", JLabel.RIGHT);
		fieldsPanel.add(coderLabel, gbc);

		gbc.gridy = 3;
		gbc.gridx = 0;
		JLabel authorLabel = new JLabel("author", JLabel.RIGHT);
		fieldsPanel.add(authorLabel, gbc);

		gbc.gridy = 4;
		gbc.gridx = 0;
		JLabel sourceLabel = new JLabel("source", JLabel.RIGHT);
		fieldsPanel.add(sourceLabel, gbc);

		gbc.gridy = 5;
		gbc.gridx = 0;
		JLabel sectionLabel = new JLabel("section", JLabel.RIGHT);
		fieldsPanel.add(sectionLabel, gbc);

		gbc.gridy = 6;
		gbc.gridx = 0;
		JLabel typeLabel = new JLabel("type", JLabel.RIGHT);
		fieldsPanel.add(typeLabel, gbc);

		gbc.gridy = 7;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		JLabel notesLabel = new JLabel("notes", JLabel.RIGHT);
		fieldsPanel.add(notesLabel, gbc);

		gbc.gridy = 8;
		gbc.gridx = 0;
		JLabel textLabel = new JLabel("text", JLabel.RIGHT);
		fieldsPanel.add(textLabel, gbc);

		// second column: fields
		gbc.insets = new Insets(3, 0, 3, 0);
		gbc.gridy = 0;
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		titleField = new JXTextField("paste the title of the document here using Ctrl-V...");
		titleField.setColumns(50);
		fieldsPanel.add(titleField, gbc);

		if (numDocuments < 2) {
			titleField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkButton();
				}
				public void checkButton() {
					boolean problem = false;
					if (titleField.getText().equals("")) {
						problem = true;
					}
					if (problem == true) {
						okButton.setEnabled(false);
					} else {
						okButton.setEnabled(true);
					}
				}
			});
		}
		
		gbc.gridy = 1;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd MM yyyy");
        dateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
        TimePickerSettings timeSettings = new TimePickerSettings();
        timeSettings.setFormatForDisplayTime("HH:mm");
        timeSettings.setFormatForMenuTimes("HH:mm");
        DateTimePicker dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
        dateTimePicker.getDatePicker().setDateToToday();
        dateTimePicker.getTimePicker().setTime(LocalTime.MIDNIGHT);
		ImageIcon dateIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-calendar-event.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        JButton dateButton = dateTimePicker.getDatePicker().getComponentToggleCalendarButton();
        dateButton.setText("");
        dateButton.setIcon(dateIcon);
		ImageIcon timeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-clock.png")).getImage().getScaledInstance(16, 16, Image.SCALE_DEFAULT));
        JButton timeButton = dateTimePicker.getTimePicker().getComponentToggleTimeMenuButton();
        timeButton.setText("");
        timeButton.setIcon(timeIcon);
        fieldsPanel.add(dateTimePicker, gbc);
		
		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JPanel coderPanel = new CoderBadgePanel();
		fieldsPanel.add(coderPanel, gbc);

		gbc.gridy = 3;
		gbc.gridx = 1;
		authorBox = new JXComboBox();
		authorBox.setEditable(true);
		JDBCWorker workerAuthor = new JDBCWorker("Author");
        workerAuthor.execute();
		authorBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(authorBox);
		fieldsPanel.add(authorBox, gbc);
		
		gbc.gridy = 4;
		gbc.gridx = 1;
		sourceBox = new JXComboBox();
		JDBCWorker workerSource = new JDBCWorker("Source");
        workerSource.execute();
		sourceBox.setEditable(true);
		sourceBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sourceBox);
		fieldsPanel.add(sourceBox, gbc);

		gbc.gridy = 5;
		gbc.gridx = 1;
		sectionBox = new JXComboBox();
		JDBCWorker workerSection = new JDBCWorker("Section");
        workerSection.execute();
		sectionBox.setEditable(true);
		sectionBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(sectionBox);
		fieldsPanel.add(sectionBox, gbc);
		
		gbc.gridy = 6;
		gbc.gridx = 1;
		typeBox = new JXComboBox();
		JDBCWorker workerType = new JDBCWorker("Type");
        workerType.execute();
		typeBox.setEditable(true);
		typeBox.setSelectedItem("");
		AutoCompleteDecorator.decorate(typeBox);
		fieldsPanel.add(typeBox, gbc);

		gbc.gridy = 7;
		gbc.gridx = 1;
		notesArea = new JXTextArea("notes...");
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setRows(4);
		JScrollPane notesScroller = new JScrollPane(notesArea);
		notesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fieldsPanel.add(notesScroller, gbc);
		
		gbc.gridy = 8;
		gbc.gridx = 1;
		fieldsPanel.add(textScroller, gbc);
		
		newArticlePanel = new JPanel(new BorderLayout());
		newArticlePanel.add(fieldsPanel, BorderLayout.NORTH);

		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
		JPanel buttons = new JPanel(fl);
		ImageIcon cancelIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		JButton cancelButton = new JButton("Cancel", cancelIcon);
		cancelButton.setToolTipText("close this window without making any changes");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		buttons.add(cancelButton);
		
		ImageIcon okIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
		String okString = "Add";
		if (numDocuments > 0) {
			okString = "Update";
		}
		okButton = new JButton(okString, okIcon);
		if (numDocuments == 0) {
			okButton.setToolTipText("insert a new article based on the information you entered in this window");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String text = textArea.getText();
					String title = titleField.getText();
					LocalDateTime dateTime = dateTimePicker.getDateTimeStrict();
					String author = (String) authorBox.getModel().getSelectedItem();
					String source = (String) sourceBox.getModel().getSelectedItem();
					String section = (String) sectionBox.getModel().getSelectedItem();
					String notes = notesArea.getText();
					String type = (String) typeBox.getModel().getSelectedItem();
					
					ArrayList<Document> al = new ArrayList<Document>();
					Document d = new Document(-1, Dna.sql.getConnectionProfile().getCoderId(), title, text, author, source, section, type, notes, dateTime, new ArrayList<Statement>());
					al.add(d);
					documents = al;
					dispose();
				}
			});
			okButton.setEnabled(false);
		} else {
			okButton.setToolTipText("save updated document data into the database");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String message = "Are you sure you want to recode " + documentIds.length + " documents and save the changes to the database?";
					int dialog = JOptionPane.showConfirmDialog(null, message, "Confirmation required", JOptionPane.YES_NO_OPTION);
					if (dialog == 0) {
						Dna.sql.updateDocuments(documentIds, titleField.getText(), textArea.getText(), (String) authorBox.getSelectedItem(), (String) sourceBox.getSelectedItem(), (String) sectionBox.getSelectedItem(), (String) typeBox.getSelectedItem(), notesArea.getText(), dateTimePicker.getDateTimeStrict());
					}
					dispose();
				}
			});
		}
		buttons.add(okButton);
		newArticlePanel.add(buttons, BorderLayout.SOUTH);
		
		// add contents for editing if this is not a new document
		if (numDocuments > 0) {
			String contentTitle = documents.get(0).getTitle();
			titleField.setText(contentTitle);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getTitle().equals(contentTitle)) {
						titleField.setText("%title");
						titleField.setPrompt("(Overwrite all titles at once by replacing this text.)");
						break;
					}
				}
			}
			String contentText = documents.get(0).getText();
			textArea.setText(contentText);
			if (Dna.sql.documentsContainStatements(documentIds) == true) {
				textArea.setEditable(false);
			}
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getText().equals(contentText)) {
						textArea.setText("%text");
						textArea.setPrompt("(Overwrite all texts at once by replacing this text.)");
						break;
					}
				}
			}
			String contentNotes = documents.get(0).getNotes();
			notesArea.setText(contentNotes);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getNotes().equals(contentNotes)) {
						notesArea.setText("%notes");
						notesArea.setPrompt("(Overwrite all notes at once by replacing this text.)");
						break;
					}
				}
			}
			String contentAuthor = documents.get(0).getAuthor();
			authorBox.setSelectedItem(contentAuthor);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getAuthor().equals(contentAuthor)) {
						authorBox.setSelectedItem("%author");
						break;
					}
				}
			}
			String contentSource = documents.get(0).getSource();
			sourceBox.setSelectedItem(contentSource);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getSource().equals(contentSource)) {
						sourceBox.setSelectedItem("%source");
						break;
					}
				}
			}
			String contentSection = documents.get(0).getSection();
			sectionBox.setSelectedItem(contentSection);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getSection().equals(contentSection)) {
						sectionBox.setSelectedItem("%section");
						break;
					}
				}
			}
			String contentType = documents.get(0).getType();
			typeBox.setSelectedItem(contentType);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getType().equals(contentType)) {
						typeBox.setSelectedItem("%type");
						break;
					}
				}
			}
			int contentCoder = documents.get(0).getCoder();
			coderPanel = new CoderBadgePanel(Dna.sql.getCoder(contentCoder));
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (documents.get(i).getCoder() != contentCoder) {
						coderPanel = new CoderBadgePanel(new Coder(-1, "(multiple coders)", new Color(0, 0, 0, 0)));
						break;
					}
				}
			}
			LocalDateTime contentDateTime = documents.get(0).getDateTime();
			dateTimePicker.setDateTimeStrict(contentDateTime);
			if (numDocuments > 1) {
				for (int i = 0; i < documents.size(); i++) {
					if (!documents.get(i).getDateTime().equals(contentDateTime)) {
						dateTimePicker.clear();
						break;
					}
				}
			}
		}
		
		this.add(newArticlePanel);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Swing worker to populate the author, source, section, and type combo
	 * boxes without blocking the event thread and GUI.
	 * 
	 * https://stackoverflow.com/questions/43161033/cant-add-tablerowsorter-to-jtable-produced-by-swingworker
	 */
	private class JDBCWorker extends SwingWorker<List<String>, String> {
		String field;
		
		public JDBCWorker(String field) {
			this.field = field;
		}
		
        @Override
        protected List<String> doInBackground() {
        	try (Connection conn = Dna.sql.getDataSource().getConnection();
        			PreparedStatement s = conn.prepareStatement("SELECT DISTINCT " + field + " FROM DOCUMENTS WHERE " + field + " IS NOT NULL ORDER BY " + field + ";")) {
        		ResultSet result = s.executeQuery();
    			while (result.next()) {
    				publish(result.getString(field));
    			}
			} catch (SQLException e) {
				System.err.println("Could not establish connection to database to retrieve document field entries.");
				e.printStackTrace();
			}
			return null;
        }
    	
        @SuppressWarnings("unchecked")
		@Override
        protected void process(List<String> chunks) {
    		int w = authorBox.getWidth();
    		int h = authorBox.getHeight();
            for (String row : chunks) {
            	if (field.equals("Author")) {
                    authorBox.addItem(row);
            	} else if (field.equals("Source")) {
            		sourceBox.addItem(row);
            	} else if (field.equals("Section")) {
            		sectionBox.addItem(row);
            	} else if (field.equals("Type")) {
            		typeBox.addItem(row);
            	}
            }
            authorBox.setPreferredSize(new Dimension(w, h));
            sourceBox.setPreferredSize(new Dimension(w, h));
            sectionBox.setPreferredSize(new Dimension(w, h));
            typeBox.setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void done() {
        	// nothing to do
        }
    }
}