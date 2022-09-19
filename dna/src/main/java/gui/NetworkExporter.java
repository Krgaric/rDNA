package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

import dna.Dna;
import export.Exporter;
import logger.LogEvent;
import logger.Logger;
import model.StatementType;
import model.TableDocument;

/**
 * GUI for network export. Allows the user to set up options for exporting
 * various kinds of networks.
 */
public class NetworkExporter extends JDialog {
	private static final long serialVersionUID = 373799108570299454L;
	private LocalDateTime[] dateTimeRange = Dna.sql.getDateTimeRange();
	private DateTimePicker startPicker, stopPicker;
	private JCheckBox helpBox;
	private JButton backboneButton, exportButton;
	private JComboBox<String> networkModesBox, fileFormatBox, var1Box, var2Box, qualifierBox, aggregationBox, normalizationBox,	isolatesBox, duplicatesBox, timeWindowBox;
	private StatementType[] statementTypes;
	private JComboBox<StatementType> statementTypeBox;
	private JSpinner timeWindowSpinner;
	private JList<String> excludeVariableList, excludeValueList;
	private HashMap<String, ArrayList<String>> excludeValues;
	private ArrayList<String> excludeAuthor, excludeSource, excludeSection, excludeType;
	private JTextArea excludePreviewArea;

	/**
	 * Constructor for GUI. Opens an Exporter window, which displays the GUI for exporting network data.
	 */
	public NetworkExporter(Frame parent) {
		super(parent, "Network export", true);
		this.setTitle("Export data");
		this.setModal(true);
		ImageIcon networkIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-affiliate.png"));
		this.setIconImage(networkIcon.getImage());
		this.setLayout(new java.awt.BorderLayout());
		
		JPanel settingsPanel = new JPanel();
		java.awt.GridBagLayout g = new java.awt.GridBagLayout();
		settingsPanel.setLayout(g);
		java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		
		// first row of options
		JLabel networkModesLabel = new JLabel("Type of network");
		String networkModesToolTip = "<html><p width=\"500\">Select the type of network to export. A <strong>one-mode "
				+ "network</strong> has the same nodes in the rows and columns of the resulting matrix (e.g., "
				+ "organizations x organizations). A <strong>two-mode network</strong> has different sets of nodes in the "
				+ "rows and columns of the resulting matrix (e.g., organizations x concepts). An <strong>event list</strong> "
				+ "is a time-stamped list of edges, with each row of the resulting table containing all variables of a "
				+ "statement including the time.</p></html>";
		networkModesLabel.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesLabel, gbc);
		
		gbc.gridx = 1;
		JLabel statementTypeLabel = new JLabel("Statement type");
		String statementTypeToolTip = 
				"<html><p width=\"500\">A network is constructed based on the variables from a specific statement type. "
				+ "Select here which statement type to use.</p></html>";
		statementTypeLabel.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeLabel, gbc);
		
		gbc.gridx = 2;
		JLabel fileFormatLabel = new JLabel("File format");
		String fileFormatToolTip = "<html><p width=\"500\">Select the file format in which the resulting network is saved. "
				+ "<strong>CSV</strong> files can be read by most programs, including spreadsheet software like Microsoft "
				+ "Excel. <strong>DL</strong> files are plain text files which can be imported by the network analysis "
				+ "software Ucinet. <strong>GRAPHML</strong> files can be opened using the network visualization software "
				+ "visone. Note that the flavor of GRAPHML used here is specific to visone.</p></html>";
		fileFormatLabel.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		String[] networkModesItems = new String[] {"Two-mode network", "One-mode network", "Event list"};
		networkModesBox = new JComboBox<>(networkModesItems);
		networkModesBox.setToolTipText(networkModesToolTip);
		settingsPanel.add(networkModesBox, gbc);
		networkModesBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String selected = (String) networkModesBox.getSelectedItem();
				if (selected.equals("Two-mode network")) {
					
					String fileFormatBackup = (String) fileFormatBox.getSelectedItem();
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					fileFormatBox.setSelectedItem(fileFormatBackup);
					
					String aggregationBackup = (String) aggregationBox.getSelectedItem();
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("combine");
					aggregationBox.addItem("subtract");
					if (aggregationBackup.equals("ignore")) {
						aggregationBox.setSelectedItem("ignore");
					} else if (aggregationBackup.equals("combine")) {
						aggregationBox.setSelectedItem("combine");
					} else if (aggregationBackup.equals("subtract")) {
						aggregationBox.setSelectedItem("subtract");
					}
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("activity");
					normalizationBox.addItem("prominence");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
					isolatesBox.addItem("include isolates");
				} else if (selected.equals("One-mode network")) {
					String fileFormatBackup = (String) fileFormatBox.getSelectedItem();
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					fileFormatBox.addItem(".dl");
					fileFormatBox.addItem(".graphml");
					fileFormatBox.setSelectedItem(fileFormatBackup);

					String aggregationBackup = (String) aggregationBox.getSelectedItem();
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
					if (aggregationBackup.equals("ignore")) {
						aggregationBox.setSelectedItem("ignore");
					} else if (aggregationBackup.equals("congruence")) {
						aggregationBox.setSelectedItem("congruence");
					} else if (aggregationBackup.equals("conflict")) {
						aggregationBox.setSelectedItem("conflict");
					} else if (aggregationBackup.equals("subtract")) {
						aggregationBox.setSelectedItem("subtract");
					}
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					normalizationBox.addItem("average");
					normalizationBox.addItem("jaccard");
					normalizationBox.addItem("cosine");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
					isolatesBox.addItem("include isolates");
				} else if (selected.equals("Event list")) {
					fileFormatBox.removeAllItems();
					fileFormatBox.addItem(".csv");
					
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					
					normalizationBox.removeAllItems();
					normalizationBox.addItem("no");
					
					isolatesBox.removeAllItems();
					isolatesBox.addItem("only current nodes");
				}
			}
		});

		gbc.gridx = 1;
		this.statementTypes = Dna.sql.getStatementTypes()
				.stream()
				.filter(s -> s.getVariables()
						.stream()
						.filter(v -> v.getDataType().equals("short text"))
						.count() > 1)
				.sorted((s1, s2) -> Integer.valueOf(s1.getId()).compareTo(Integer.valueOf(s2.getId())))
				.toArray(StatementType[]::new);
		if (statementTypes.length < 1) {
			LogEvent l = new LogEvent(Logger.ERROR,
					"[GUI] No suitable statement types for network export.",
					"The network export dialog window has tried to populate the statement type combo box with statement types, but there was not a single statement type containing at least two short text variables that could be used for network construction.");
			Dna.logger.log(l);
		}
		StatementTypeComboBoxRenderer cbrenderer = new StatementTypeComboBoxRenderer();
		statementTypeBox = new JComboBox<StatementType>(statementTypes);
		statementTypeBox.setRenderer(cbrenderer);
		statementTypeBox.setSelectedIndex(0);
		statementTypeBox.setToolTipText(statementTypeToolTip);
		settingsPanel.add(statementTypeBox, gbc);
		final int HEIGHT = (int) statementTypeBox.getPreferredSize().getHeight();
		final int WIDTH = 200;
		java.awt.Dimension d = new java.awt.Dimension(WIDTH, HEIGHT);
		networkModesBox.setPreferredSize(d);
		statementTypeBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				StatementType selected = (StatementType) statementTypeBox.getSelectedItem();
				String[] varItems = NetworkExporter.this.getVariablesList(selected, false, true, false, false);
				String[] qualifierItems = NetworkExporter.this.getVariablesList(selected, false, false, true, true);
				var1Box.removeAllItems();
				var2Box.removeAllItems();
				qualifierBox.removeAllItems();
				if (varItems.length > 0) {
					for (int i = 0; i < varItems.length; i++) {
						var1Box.addItem(varItems[i]);
						var2Box.addItem(varItems[i]);
					}
				}
				if (qualifierItems.length > 0) {
					for (int i = 0; i < qualifierItems.length; i++) {
						qualifierBox.addItem(qualifierItems[i]);
					}
				}
				populateExcludeVariableList();
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
			}
		});
		
		gbc.gridx = 2;
		String[] fileFormatItems = new String[] {".csv", ".dl", ".graphml"};
		fileFormatBox = new JComboBox<>(fileFormatItems);
		fileFormatBox.setToolTipText(fileFormatToolTip);
		settingsPanel.add(fileFormatBox, gbc);
		fileFormatBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
		statementTypeBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
		
		// second row of options
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		JLabel var1Label = new JLabel("Variable 1");

		String var1ToolTip = "<html><p width=\"500\">In a one-mode network, the first variable denotes the node class used "
				+ "both for the rows and columns of the matrix. For example, select the variable for organizations in order "
				+ "to export an organization x organization network. In a two-mode network, the first variable denotes the "
				+ "node class for the rows. For example, select the variable for organizations here if you want to export "
				+ "an organization x concept two-mode network.</p></html>";
		var1Label.setToolTipText(var1ToolTip);
		settingsPanel.add(var1Label, gbc);
		
		gbc.gridx = 1;
		JLabel var2Label = new JLabel("Variable 2");
		String var2ToolTip = "<html><p width=\"500\">In a one-mode network, the second variable denotes the variable through "
				+ "which the edges are aggregated. For example, if you export a one-mode network of organizations and the "
				+ "edge weight that connects any two organizations should denote the two organizations' number of joint "
				+ "concepts, then the second variable should denote the concepts. In a two-mode network, the second variable "
				+ "denotes the node class used for the columns of the resulting network matrix. For example, one would select "
				+ "the variable for concepts here in order to export an organization x concept network.</p></html>";
		var2Label.setToolTipText(var2ToolTip);
		settingsPanel.add(var2Label, gbc);
		
		gbc.gridx = 2;
		JLabel qualifierLabel = new JLabel("Qualifier");
		String qualifierToolTip = "<html><p width=\"500\">The qualifier is a binary or integer variable which indicates "
				+ "different qualities or levels of association between variable 1 and variable 2. If a binary qualifier "
				+ "variable is used, this amounts to positive versus negative relations or agreement versus disagreement. "
				+ "For example, in an organization x concept two-mode network, a qualifier variable could indicate whether "
				+ "an organization supports or rejects a concept. The qualifier would indicate support or rejection in this "
				+ "case. The qualifier is also preserved in a one-mode network. For example, in an organization x organization "
				+ "network, any two organizations can be connected if they either co-support or co-reject the same concept. "
				+ "In other words, a one-mode projection of the support two-mode network and a one-mode projection of the "
				+ "rejection two-mode network are stacked and added in order to obtain a congruence network. If an integer "
				+ "(non-binary) qualifier variable is used, this indicates different intensities in a two-mode network, for "
				+ "example the extent of support (in the positive range) or rejection (in the negative range) of a concept by "
				+ "an organization. In a one-mode network, differences in these levels of intensity are used as edge weights; "
				+ "the description under Qualifier aggregation contains more details on this.</p></html>";
		qualifierLabel.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierLabel, gbc);
		
		gbc.gridx = 3;
		JLabel aggregationLabel = new JLabel("Qualifier aggregation");
		String aggregationToolTip = "<html><p width=\"500\">The choices differ between one-mode and two-mode networks. <strong>"
				+ "ignore</strong> is available in both cases and means that the agreement qualifier variable is ignored, i.e., "
				+ "the network is constructed as if all values on the qualifier variable were the same. In the two-mode network "
				+ "case, <strong>subtract</strong> means that negative absolute values on the qualifier variable are subtracted from "
				+ "positive values (if an integer qualifier is selected) or that 0 values are subtracted from 1 values (if a "
				+ "binary qualifier variable is selected). For example, if an organization mentions a concept two times in a "
				+ "positive way and three times in a negative way, there will be an edge weight of -1 between the organization "
				+ "and the concept. In the one-mode case, <strong>subtract</strong> means that a congruence network and a "
				+ "conflict network are created separately and then the (potentially normalized) conflict network ties are "
				+ "subtracted from the (potentially normalized) congruence network ties. For example, if two organizations A "
				+ "and B disagree over 3 concepts and agree with regard to 5 concepts, they have an edge weight of 2. <strong>"
				+ "combine</strong> is only available for two-mode networks. In contrast to the subtract option, it means that "
				+ "the values of the qualifier are treated as qualitative categories. In this case, DNA creates all possible "
				+ "combinations of edges (e.g., in the binary case, these are support, rejection, and both/mixed/ambiguous). "
				+ "Integer values are then used as edge weights, as in a multiplex network. E.g., 1 represents support, 2 "
				+ "represents rejection, and 3 represents both. With an integer variable, this may become more complex. In "
				+ "one-mode networks, <strong>congruence</strong> means that similarity or matches on the qualifier variable "
				+ "are counted in order to construct an edge. With a binary variable, matches are used. For example, if "
				+ "organizations A and B both co-support or both co-reject concept C, they are connected, and the number of "
				+ "co-supported and co-rejected concepts is used as the edge weight. With an integer qualifier variable, "
				+ "the inverse of the absolute distance plus one (i.e., the proximity) is used instead of a match. In one-mode "
				+ "networks, <strong>conflict</strong> constructs the network by recording disagreements between actors, rather "
				+ "than agreements, as in the congruence case. With a binary qualifier, this means that organizations A and B "
				+ "are connected if one of them supports a concept and the other one rejects the concept. With an integer "
				+ "qualifier, the absolute distance is used instead.</p></html>";
		aggregationLabel.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationLabel, gbc);
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 3;
		String[] varItems = NetworkExporter.this.getVariablesList((StatementType) statementTypeBox.getSelectedItem(), false, true, false, false);
		var1Box = new JComboBox<String>(varItems);
		var1Box.setToolTipText(var1ToolTip);
		var1Box.setSelectedIndex(0);
		settingsPanel.add(var1Box, gbc);
		int HEIGHT2 = (int) var1Box.getPreferredSize().getHeight();
		var1Box.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		var2Box = new JComboBox<String>(varItems);
		var2Box.setToolTipText(var2ToolTip);
		var2Box.setSelectedIndex(1);
		settingsPanel.add(var2Box, gbc);
		var2Box.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		java.awt.event.ActionListener varActionListener = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (var1Box.getItemCount() < 1 || var2Box.getItemCount() < 1) {
					var1Box.setBorder(null);
					var2Box.setBorder(null);
				} else if (var1Box.getSelectedItem().equals(var2Box.getSelectedItem())) {
					var1Box.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED));
					var2Box.setBorder(BorderFactory.createLineBorder(java.awt.Color.RED));
				} else {
					var1Box.setBorder(null);
					var2Box.setBorder(null);
				}
			}
		};
		var1Box.addActionListener(varActionListener);
		var2Box.addActionListener(varActionListener);
		
		gbc.gridx = 2;
		String[] qualifierItems = getVariablesList((StatementType) statementTypeBox.getSelectedItem(), false, false, true, true);
		qualifierBox = new JComboBox<>(qualifierItems);
		qualifierBox.setToolTipText(qualifierToolTip);
		settingsPanel.add(qualifierBox, gbc);
		qualifierBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		qualifierBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (qualifierBox.getItemCount() == 0) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
				} else if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("combine");
					aggregationBox.addItem("subtract");
				} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
					aggregationBox.addItem("congruence");
					aggregationBox.addItem("conflict");
					aggregationBox.addItem("subtract");
				} else if (networkModesBox.getSelectedItem().equals("Event list")) {
					aggregationBox.removeAllItems();
					aggregationBox.addItem("ignore");
				}
			}
		});

		gbc.gridx = 3;
		String[] aggregationItems = new String[] {"ignore", "combine", "subtract"};
		aggregationBox = new JComboBox<>(aggregationItems);
		aggregationBox.setToolTipText(aggregationToolTip);
		settingsPanel.add(aggregationBox, gbc);
		aggregationBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		aggregationBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (aggregationBox.getItemCount() > 0) {
					if (aggregationBox.getSelectedItem().equals("combine")) {
						normalizationBox.removeAllItems();
						normalizationBox.addItem("no");
					} else {
						if (networkModesBox.getSelectedItem().equals("Two-mode network")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
							normalizationBox.addItem("activity");
							normalizationBox.addItem("prominence");
						} else if (networkModesBox.getSelectedItem().equals("One-mode network")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
							normalizationBox.addItem("average");
							normalizationBox.addItem("jaccard");
							normalizationBox.addItem("cosine");
						} else if (networkModesBox.getSelectedItem().equals("Event list")) {
							normalizationBox.removeAllItems();
							normalizationBox.addItem("no");
						}
					}
				}
			}
		});
		
		
		// third row of options
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 4;
		JLabel normalizationLabel = new JLabel("Normalization");
		String normalizationToolTip = "<html><p width=\"500\">Normalization of edge weights serves to cancel out the effect "
				+ "that some nodes are more active than others. For example, in an organization x organization one-mode "
				+ "network aggregated through a concept variable, some organizations with an institutional mandate end "
				+ "up in very central locations of the network because they have agreement with many other actors due to "
				+ "their role rather than their actual opinion, i.e., because their office requires them to reveal their "
				+ "preferences on many concepts. To cancel out this activity effect, one can divide the edge weight "
				+ "between two organizations by a function of the two incident organizations' activity or frequency of "
				+ "making statements. This normalization procedure leaves us with a network of edge weights that reflect "
				+ "similarity in opinions without taking into account centrality or activity in any way. There are several "
				+ "ways to normalize a one-mode network. <strong>No normalization</strong> switches off normalization and "
				+ "is the default value. <strong>Average activity</strong> divides edge weights between first-variable "
				+ "nodes by the average number of different second-variable nodes they are adjacent with in a two-mode "
				+ "network. For example, if organization A makes statements about 20 different concepts and B makes "
				+ "statements about 60 difference concepts, the edge weight between A and B in the congruence network "
				+ "is divided by 40. To achieve a better scaling, all edge weights in the resulting normalized one-mode "
				+ "network matrix are scaled between 0 and 1. <strong>Jaccard similarity</strong> is a similarity measure "
				+ "with known normalizing properties as well. Rather than average activity, it divides the co-occurrence "
				+ "frequency by the activity count of both separate actors plus their joint activity. <strong>Cosine "
				+ "similarity </strong>is another similarity measure with normalizing properties. It divides edge weights "
				+ "by the product of the nodes' activity. Details on these normalization measures can be found in <emph>"
				+ "Leifeld, Philip (2017): Discourse Network Analysis: Policy Debates as Dynamic Networks. In: Oxford "
				+ "Handbook of Political Networks, edited by JN Victor, AH Montgomery, and MN Lubell, chapter 25, Oxford "
				+ "University Press.</emph> With two-mode networks, <strong>activity</strong> normalization and <strong>"
				+ "prominence</strong> normalization are available. They divide the edge weights through the activity of "
				+ "the node from the first or the second mode, respectively.</p></html>";
		normalizationLabel.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationLabel, gbc);
		
		gbc.gridx = 1;
		JLabel isolatesLabel = new JLabel("Isolates");
		String isolatesToolTip = "<html><p width=\"500\">Often, one would like to export several time slices or networks "
				+ "for different qualifier values separately etc. and merge the resulting networks later on manually. "
				+ "In these situations, it is easier to merge multiple networks if they have the same matrix dimensions. "
				+ "However, this is usually not the case because the network export functions by default only include "
				+ "nodes that show at least minimal activity in the network that is exported. To achieve compatibility of "
				+ "the matrix dimensions anyway, it is possible to include all nodes of the selected variable(s) in the "
				+ "whole database, irrespective of time, qualifiers, and excluded values (but without any edge weights "
				+ "larger than 0, i.e., as isolates). This is meant by including isolates.</p></html>";
		isolatesLabel.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesLabel, gbc);

		gbc.gridx = 2;
		JLabel duplicatesLabel = new JLabel("Duplicates");
		String duplicatesToolTip = "<html><p width=\"500\">By default, all statements are included in a network export. "
				+ "However, in many data sources, repeated identical statements within the same document are an "
				+ "artifact of the source material. For example, in a newspaper article, the number of times an actor "
				+ "is quoted with a statement may be a function of the journalist's agenda or the reporting style of "
				+ "the news media outlet, rather than the actor's deliberate attempt to speak multiple times. In cases "
				+ "like this it makes sense to count each statement only once per document. Alternatively, it may "
				+ "make sense to count statements only once per week, month, or year or in the whole time period if this "
				+ "artifact happens across documents (e.g., if a newspaper reprints interviews or reports on the same "
				+ "press release multiple times in different documents).</p></html>";
		duplicatesLabel.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesLabel, gbc);

		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 5;
		String[] normalizationItems = new String[] {"no", "activity", "prominence"};
		normalizationBox = new JComboBox<>(normalizationItems);
		normalizationBox.setToolTipText(normalizationToolTip);
		settingsPanel.add(normalizationBox, gbc);
		normalizationBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		gbc.gridx = 1;
		String[] isolatesItems = new String[] {"only current nodes", "include isolates"};
		isolatesBox = new JComboBox<>(isolatesItems);
		isolatesBox.setToolTipText(isolatesToolTip);
		settingsPanel.add(isolatesBox, gbc);
		isolatesBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		isolatesBox.addItemListener(il -> {
			if (isolatesBox.getSelectedItem() != null && ((String) isolatesBox.getSelectedItem()).equals("include isolates") && ((String) networkModesBox.getSelectedItem()).equals("One-mode network") && ((String) timeWindowBox.getSelectedItem()).equals("no time window")) {
				backboneButton.setEnabled(true);
			} else {
				backboneButton.setEnabled(false);
			}
		});

		gbc.gridx = 2;
		String[] duplicatesItems = new String[] {"include all duplicates", "ignore per document", "ignore per calendar week", 
				"ignore per calendar month", "ignore per calendar year", "ignore across date range"};
		duplicatesBox = new JComboBox<>(duplicatesItems);
		duplicatesBox.setToolTipText(duplicatesToolTip);
		settingsPanel.add(duplicatesBox, gbc);
		duplicatesBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		// fourth row of options
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 6;
		JLabel startLabel = new JLabel("Include from");
		String dateToolTip = "<html><p width=\"500\">All statements before the start date and time and after the stop date "
				+ "and time will be ignored during network export. This can be helpful for exporting multiple time slices "
				+ "and analyzing them separately. By default, the first and last date/time stamp in any statement found in "
				+ "the whole database are used, i.e., the default setting includes all statements.</p></html>";
		startLabel.setToolTipText(dateToolTip);
		settingsPanel.add(startLabel, gbc);
		
		gbc.gridx = 1;
		JLabel stopLabel = new JLabel("Include until");
		stopLabel.setToolTipText(dateToolTip);
		settingsPanel.add(stopLabel, gbc);

		gbc.gridx = 2;
		JLabel timeWindowLabel = new JLabel("Moving time window");
		String timeWindowToolTip = "<html><p width=\"500\">Create multiple overlapping time slices that are moved "
				+ "forward along the time axis. For example, if a time window size of 100 days is used and the time "
				+ "axis starts on a certain start date and ends on a certain end date, then the first time slice will "
				+ "comprise all statements between the start date and 100 days later. E.g., a one-mode network or a "
				+ "two-mode network for this time period is created. Then the time window is shifted by one time unit, "
				+ "and a new network is created for the second to the 101st day, and so forth until the end of the time "
				+ "period is reached. All time slices are saved as separate networks. The networks will be saved to new "
				+ "files each time, with an iterator number added to the file name. Instead "
				+ "of days, the user can select other time units by which the time window is shifted. However, the "
				+ "time slice is always moved forward by one time unit. To get mutually exclusive (i.e., "
				+ "non-overlapping) time slices, the user should select them manually from the output. For example, if "
				+ "100 days are used, one could select the first time slice, the 101st slice, the 201st time slice and "
				+ "so on. Instead of time units, it is also possible to use event time. This will create time slices "
				+ "of exactly 100 statement events, for example. However, it is possible that multiple events have "
				+ "identical time steps. In this case, the resulting network time slice is more inclusive and also "
				+ "contains those statements that happened at the same time.</p></html>";
		timeWindowLabel.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowLabel, gbc);
		
		gbc.gridx = 3;
		JLabel timeWindowNumberLabel = new JLabel("Time window size");
		timeWindowNumberLabel.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowNumberLabel, gbc);
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 7;
		
		ImageIcon dateIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-calendar-event.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		ImageIcon timeIcon = new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-clock.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        
		DatePickerSettings startDateSettings = new DatePickerSettings();
        startDateSettings.setFormatForDatesCommonEra("dd MM yyyy");
        startDateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
        TimePickerSettings startTimeSettings = new TimePickerSettings();
        startTimeSettings.setFormatForDisplayTime("HH:mm:ss");
        startTimeSettings.setFormatForMenuTimes("HH:mm:ss");

        startPicker = new DateTimePicker(startDateSettings, startTimeSettings);
        startPicker.setDateTimeStrict(dateTimeRange[0]);
		JButton startDateButton = startPicker.getDatePicker().getComponentToggleCalendarButton();
        startDateButton.setText("");
        startDateButton.setIcon(dateIcon);
        startDateButton.setToolTipText(dateToolTip);
		JButton startTimeButton = startPicker.getTimePicker().getComponentToggleTimeMenuButton();
        startTimeButton.setText("");
        startTimeButton.setIcon(timeIcon);
        startTimeButton.setToolTipText(dateToolTip);
        startPicker.setToolTipText(dateToolTip);
		startPicker.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		settingsPanel.add(startPicker, gbc);

        DatePickerSettings stopDateSettings = new DatePickerSettings();
        stopDateSettings.setFormatForDatesCommonEra("dd MM yyyy");
        stopDateSettings.setFormatForDatesBeforeCommonEra("dd MM uuuu");
        TimePickerSettings stopTimeSettings = new TimePickerSettings();
        stopTimeSettings.setFormatForDisplayTime("HH:mm:ss");
        stopTimeSettings.setFormatForMenuTimes("HH:mm:ss");
        
		gbc.gridx = 1;
        stopPicker = new DateTimePicker(stopDateSettings, stopTimeSettings);
        stopPicker.setDateTimeStrict(dateTimeRange[1]);
		JButton stopDateButton = stopPicker.getDatePicker().getComponentToggleCalendarButton();
        stopDateButton.setText("");
        stopDateButton.setIcon(dateIcon);
        stopDateButton.setToolTipText(dateToolTip);
		JButton stopTimeButton = stopPicker.getTimePicker().getComponentToggleTimeMenuButton();
        stopTimeButton.setText("");
        stopTimeButton.setIcon(timeIcon);
        stopTimeButton.setToolTipText(dateToolTip);
        stopPicker.setToolTipText(dateToolTip);
		stopPicker.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		settingsPanel.add(stopPicker, gbc);
		
		gbc.gridx = 2;
		String[] timeWindowItems = new String[] {"no time window", "using events", "using seconds", "using minutes", 
				"using hours", "using days", "using weeks", "using months", "using years"};
		timeWindowBox = new JComboBox<>(timeWindowItems);
		timeWindowBox.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowBox, gbc);
		timeWindowBox.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		timeWindowBox.addItemListener(il -> {
			if (((String) isolatesBox.getSelectedItem()).equals("include isolates") && ((String) networkModesBox.getSelectedItem()).equals("One-mode network") && ((String) timeWindowBox.getSelectedItem()).equals("no time window")) {
				backboneButton.setEnabled(true);
			} else {
				backboneButton.setEnabled(false);
			}
		});
		
		gbc.gridx = 3;
		timeWindowSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100000, 1));
		timeWindowSpinner.setToolTipText(timeWindowToolTip);
		settingsPanel.add(timeWindowSpinner, gbc);
		timeWindowSpinner.setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT2));
		
		// fifth row of options: exclude values from variables
		gbc.insets = new java.awt.Insets(10, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 8;
		JLabel excludeVariableLabel = new JLabel("Exclude from variable");
		String excludeToolTip = "<html><p width=\"500\">By default, all nodes from all statements are included in the "
				+ "network, given the type of network and the statement type and variables selected. Often, however, "
				+ "one wishes to exclude certain nodes or statements from the analysis. For example, in an "
				+ "organization x organization congruence network via concepts, if one already knows that a certain "
				+ "concept is very general and does not add to the structure of the discourse network, one can "
				+ "exclude this concept. Or if the analysis should be restricted to a subgroup of actors (e.g., only "
				+ "legislators), one can select multiple actors that do not belong to this subgroup, and they will be "
				+ "ignored during network export. The way this process works is that the list of statements from which "
				+ "the network is generated is first filtered, and only statements that do not contain any of the "
				+ "values to be excluded will be retained for the export. To do this, one first needs to select a "
				+ "variable on the left and then select the values to be excluded. To select or unselect multiple "
				+ "values, one can hold the ctrl key while selecting or unselecting additional values. The preview "
				+ "on the right lists all variable-value combinations that are excluded.</p></html>";
		excludeVariableLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableLabel, gbc);
		
		gbc.gridx = 1;
		JLabel excludeValueLabel = new JLabel("Exclude values");
		excludeValueLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValueLabel, gbc);
		
		gbc.gridx = 2;
		JLabel excludePreviewLabel = new JLabel("Preview of excluded values");
		excludePreviewLabel.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewLabel, gbc);
		
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		gbc.gridx = 0;
		gbc.gridy = 9;
		excludeVariableList = new JList<String>();
		excludeVariableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		excludeVariableList.setLayoutOrientation(JList.VERTICAL);
		excludeVariableList.setVisibleRowCount(10);
		JScrollPane excludeVariableScroller = new JScrollPane(excludeVariableList);
		excludeVariableScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeVariableList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeVariableScroller, gbc);
		excludeVariableScroller.setPreferredSize(new java.awt.Dimension(WIDTH, (int) excludeVariableScroller.getPreferredSize().getHeight()));
		excludeAuthor = new ArrayList<String>();
		excludeSource = new ArrayList<String>();
		excludeSection = new ArrayList<String>();
		excludeType = new ArrayList<String>();
		excludeValues = new HashMap<String, ArrayList<String>>();
		NetworkExporter.this.populateExcludeVariableList();
		excludeVariableList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedValue = excludeVariableList.getSelectedValue();
				if (!e.getValueIsAdjusting() && selectedValue != null) {
					String[] entriesArray;
					int[] indices;
					ArrayList<TableDocument> documents = Dna.sql.getTableDocuments(new int[0]);
					if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 1) { // document type variable
						entriesArray = documents
								.stream()
								.map(d -> d.getType())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeType.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeType.size(); j++) {
								if (entriesArray[i].equals(excludeType.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 2) { // document section variable
						entriesArray = documents
								.stream()
								.map(d -> d.getSection())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeSection.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeSection.size(); j++) {
								if (entriesArray[i].equals(excludeSection.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 3) { // document source variable
						entriesArray = documents
								.stream()
								.map(d -> d.getSource())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeSource.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeSource.size(); j++) {
								if (entriesArray[i].equals(excludeSource.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else if (excludeVariableList.getSelectedIndex() == excludeVariableList.getModel().getSize() - 4) { // document author variable
						entriesArray = documents
								.stream()
								.map(d -> d.getAuthor())
								.distinct()
								.sorted((d1, d2) -> d1.compareTo(d2))
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeAuthor.size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeAuthor.size(); j++) {
								if (entriesArray[i].equals(excludeAuthor.get(j))) {
									indices[j] = i;
								}
							}
						}
					} else {
						entriesArray = Dna.sql.getUniqueValues(((StatementType) statementTypeBox.getSelectedItem()).getId(), selectedValue)
								.stream()
								.toArray(String[]::new);
						excludeValueList.setListData(entriesArray);
						indices = new int[excludeValues.get(selectedValue).size()];
						for (int i = 0; i < entriesArray.length; i++) {
							for (int j = 0; j < excludeValues.get(selectedValue).size(); j++) {
								if (entriesArray[i].equals(excludeValues.get(selectedValue).get(j))) {
									indices[j] = i;
								}
							}
						}
					}
					excludeValueList.setSelectedIndices(indices);
				}
			}
		});
		
		gbc.gridx = 1;
		excludeValueList = new JList<String>();
		excludeValueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		excludeValueList.setLayoutOrientation(JList.VERTICAL);
		excludeValueList.setVisibleRowCount(10);
		JScrollPane excludeValueScroller = new JScrollPane(excludeValueList);
		excludeValueScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		excludeValueList.setToolTipText(excludeToolTip);
		settingsPanel.add(excludeValueScroller, gbc);
		excludeValueScroller.setPreferredSize(new java.awt.Dimension(WIDTH, (int) excludeValueScroller.getPreferredSize().getHeight()));
		excludeValueList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selectedVariable = excludeVariableList.getSelectedValue();
				List<String> selectedValues = excludeValueList.getSelectedValuesList();
				if (!e.getValueIsAdjusting()) {
					if (selectedValues != null) {
						ArrayList<String> sel = new ArrayList<String>(selectedValues);
						if (selectedVariable.equals("type")) {
							excludeType = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("source")) {
							excludeSource = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("section")) {
							excludeSection = (ArrayList<String>) sel;
						} else if (selectedVariable.equals("author")) {
							excludeAuthor = (ArrayList<String>) sel;
						} else {
							excludeValues.put(selectedVariable, (ArrayList<String>) sel);
						}
					}
					String excludePreviewText = "";
					Iterator<String> it = excludeValues.keySet().iterator();
					while (it.hasNext()) {
						String excludeVariable = it.next();
						ArrayList<String> excludeValueArrayList = excludeValues.get(excludeVariable);
						if (excludeValueArrayList.size() > 0) {
							for (int i = 0; i < excludeValueArrayList.size(); i++) {
								excludePreviewText = excludePreviewText + excludeVariable + ": " + excludeValueArrayList.get(i) + "\n";
							}
						}
					}
					if (excludeAuthor.size() > 0) {
						for (int i = 0; i < excludeAuthor.size(); i++) {
							excludePreviewText = excludePreviewText + "author: " + excludeAuthor.get(i) + "\n";
						} 
					}
					if (excludeSource.size() > 0) {
						for (int i = 0; i < excludeSource.size(); i++) {
							excludePreviewText = excludePreviewText + "source: " + excludeSource.get(i) + "\n";
						} 
					}
					if (excludeSection.size() > 0) {
						for (int i = 0; i < excludeSection.size(); i++) {
							excludePreviewText = excludePreviewText + "section: " + excludeSection.get(i) + "\n";
						} 
					}
					if (excludeType.size() > 0) {
						for (int i = 0; i < excludeType.size(); i++) {
							excludePreviewText = excludePreviewText + "type: " + excludeType.get(i) + "\n";
						} 
					}
					excludePreviewArea.setText(excludePreviewText);
				}
			}
		});
		
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		excludePreviewArea = new JTextArea();
		excludePreviewArea.setText("");
		excludePreviewArea.setEditable(false);
		excludePreviewArea.setBackground(settingsPanel.getBackground());
		JScrollPane excludePreviewScroller = new JScrollPane(excludePreviewArea);
		excludePreviewArea.setToolTipText(excludeToolTip);
		settingsPanel.add(excludePreviewScroller, gbc);
		excludePreviewScroller.setPreferredSize(excludeValueScroller.getPreferredSize());
		
		// sixth row: buttons
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 10;
		helpBox = new JCheckBox("Display instructions");
		helpBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				toggleHelp();
			}
			
		});
		settingsPanel.add(helpBox, gbc);

		gbc.gridwidth = 3;
		gbc.gridx = 1;
		gbc.anchor = java.awt.GridBagConstraints.EAST;
		JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		JButton revertButton = new JButton("Revert", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-backspace.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		
		String revertToolTip = "<html><p>Reset all settings to their default values.</p></html>";
		revertButton.setToolTipText(revertToolTip);
		buttonPanel.add(revertButton);
		revertButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				networkModesBox.setSelectedIndex(0);
				int statementTypeIndex = -1;
				for (int i = 0; i < NetworkExporter.this.statementTypes.length; i++) {
					String[] vars = NetworkExporter.this.getVariablesList(NetworkExporter.this.statementTypes[i], false, true, false, false);
					if (vars.length > 1) {
						statementTypeIndex = i;
						break;
					}
				}
				if (statementTypeIndex == -1) {
					statementTypeBox.setSelectedIndex(0);
					System.err.println("No statement type with more than one short text variable found!");
				} else {
					statementTypeBox.setSelectedIndex(statementTypeIndex);
				}
				statementTypeBox.updateUI();
				if (var1Box.getItemCount() > 1) {
					var1Box.setSelectedIndex(0);
					var2Box.setSelectedIndex(1);
				}
				if (qualifierBox.getItemCount() > 0) {
					qualifierBox.setSelectedIndex(0);
				}
				aggregationBox.setSelectedIndex(0);
				normalizationBox.setSelectedIndex(0);
				isolatesBox.setSelectedIndex(0);
				duplicatesBox.setSelectedIndex(0);
		        startPicker.setDateTimeStrict(NetworkExporter.this.dateTimeRange[0]);
		        stopPicker.setDateTimeStrict(NetworkExporter.this.dateTimeRange[1]);
				timeWindowBox.setSelectedIndex(0);
				timeWindowSpinner.setValue(100);
				excludeVariableList.setSelectedIndex(0);
				excludePreviewArea.setText("");
				helpBox.setSelected(false);
			}
		});

		JButton cancelButton = new JButton("Cancel", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-x.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String cancelToolTip = "<html><p>Reset and close this window.</p></html>";
		cancelButton.setToolTipText(cancelToolTip);
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispose();
			}
		});

		backboneButton = new JButton("Find backbone...", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-arrows-split.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String backboneToolTip = "<html><p width=\"500\">Find the backbone and redundant set of second-mode entities. " +
				"This functionality is only available with <b>one-mode networks, no isolates, and no time window</b>. " +
				"Variable 2 constitutes the second mode in an two-mode network and feeds into the edge weights of a " +
				"one-mode network, such as a normalized actor congruence or subtract network. The <b>backbone set</b> is the " +
				"subset of entities/values of Variable 2 (e.g., concepts) that can reproduce the cluster structure of " +
				"the one-mode network almost perfectly, i.e., with little loss. The <b>redundant set</b> is the complementary " +
				"subset of entities/values of Variable 2 that do not contribute much additional value in structuring " +
				"the one-mode network into clusters. A custom <b>simulated annealing</b> algorithm is employed to find the " +
				"backbone and redundant sets by minimizing penalized Euclidean spectral distances between the backbone " +
				"network and the full network. You can supply two parameters: The <b>penalty (p)</b> determines how large the " +
				"backbone set is, with larger penalties penalizing larger backbones more strongly, hence leading to " +
				"smaller backbones and larger redundant sets. Typical values could be 5.5, 7.5, or 12. The number of " +
				"<b>iterations (T)</b> determines how long the algorithm should run. We have had good results with T = 50,000 " +
				"iterations for a network of about 200 actors and about 60 concepts, though fewer iterations might have " +
				"been acceptable. The quality of the solution increases with larger T and reaches the global optimum " +
				"asymptotically. How many iterations are required for a good solution depends on the number of entities " +
				"on the second-mode variable. The results can be saved to <b>JSON or XML</b> files and can inform how to recode " +
				"entities or which entities to include or exclude during network export.</p></html>";
		backboneButton.setToolTipText(backboneToolTip);
		buttonPanel.add(backboneButton);
		backboneButton.addActionListener(e -> new BackboneDialog());
		backboneButton.setEnabled(false);

		exportButton = new JButton("Network export...", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-check.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
		String exportToolTip = "<html><p>Select a file name and save the network using the current settings.</p></html>";
		exportButton.setToolTipText(exportToolTip);
		exportButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith((String) fileFormatBox.getSelectedItem()) || f.isDirectory();
					}
					public String getDescription() {
						return "Network File (*" + (String) fileFormatBox.getSelectedItem() + ")";
					}
				});
				
				int returnVal = fc.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String fileName = file.getPath();
					if (!fileName.endsWith((String) fileFormatBox.getSelectedItem())) {
						fileName = fileName + (String) fileFormatBox.getSelectedItem();
					}
					
					// read settings from GUI elements and translate into values for Exporter class
					String networkType = (String) networkModesBox.getSelectedItem();
					if (networkType.equals("Two-mode network")) {
						networkType = "twomode";
					} else if (networkType.equals("One-mode network")) {
						networkType = "onemode";
					} else if (networkType.equals("Event list")) {
						networkType = "eventlist";
					}
					StatementType statementType = (StatementType) statementTypeBox.getSelectedItem();
					String variable1Name = (String) var1Box.getSelectedItem();
					boolean variable1Document = false;
					if (var1Box.getSelectedIndex() > var1Box.getItemCount() - 7) {
						variable1Document = true;
					}
					String variable2Name = (String) var2Box.getSelectedItem();
					boolean variable2Document = false;
					if (var2Box.getSelectedIndex() > var2Box.getItemCount() - 7) {
						variable2Document = true;
					}
					String qualifier = (String) qualifierBox.getSelectedItem();
					String qualifierAggregation = (String) aggregationBox.getSelectedItem();
					String normalization = (String) normalizationBox.getSelectedItem();
					boolean isolates = false;
					if (isolatesBox.getSelectedItem().equals("include isolates")) {
						isolates = true;
					}
					String duplicates = (String) duplicatesBox.getSelectedItem();
					if (duplicates.equals("include all duplicates")) {
						duplicates = "include";
					} else if (duplicates.equals("ignore per document")) {
						duplicates = "document";
					} else if (duplicates.equals("ignore per calendar week")) {
						duplicates = "week";
					} else if (duplicates.equals("ignore per calendar month")) {
						duplicates = "month";
					} else if (duplicates.equals("ignore per calendar year")) {
						duplicates = "year";
					} else if (duplicates.equals("ignore across date range")) {
						duplicates = "acrossrange";
					}
					LocalDateTime startDateTime = startPicker.getDateTimeStrict();
					LocalDateTime stopDateTime = stopPicker.getDateTimeStrict();
					String timeWindow = (String) timeWindowBox.getSelectedItem();
					if (timeWindow.equals("no time window")) {
						timeWindow = "no";
					} else if (timeWindow.equals("using events")) {
						timeWindow = "events";
					} else if (timeWindow.equals("using seconds")) {
						timeWindow = "seconds";
					} else if (timeWindow.equals("using minutes")) {
						timeWindow = "minutes";
					} else if (timeWindow.equals("using hours")) {
						timeWindow = "hours";
					} else if (timeWindow.equals("using days")) {
						timeWindow = "days";
					} else if (timeWindow.equals("using weeks")) {
						timeWindow = "weeks";
					} else if (timeWindow.equals("using months")) {
						timeWindow = "months";
					} else if (timeWindow.equals("using years")) {
						timeWindow = "years";
					}
					int windowSize = (int) timeWindowSpinner.getModel().getValue();
					String fileFormat = (String) fileFormatBox.getSelectedItem();
					if (fileFormat.equals(".csv")) {
						fileFormat = "csv";
					} else if (fileFormat.equals(".dl")) {
						fileFormat = "dl";
					} else if (fileFormat.equals(".graphml")) {
						fileFormat = "graphml";
					}
					
					// start export thread
					Thread exportThread = new Thread( new GuiExportThread(networkType, statementType, variable1Name,
							variable1Document, variable2Name, variable2Document, qualifier, qualifierAggregation,
							normalization, isolates, duplicates, startDateTime, stopDateTime, timeWindow, windowSize,
							NetworkExporter.this.excludeValues, NetworkExporter.this.excludeAuthor,
							NetworkExporter.this.excludeSource, NetworkExporter.this.excludeSection,
							NetworkExporter.this.excludeType, false, false, false, false, false, fileFormat, fileName),
							"Export network" );
					exportThread.start();
				}
			}
		});
		buttonPanel.add(exportButton);
		toggleHelp();
		settingsPanel.add(buttonPanel, gbc);
		
		this.add(settingsPanel, java.awt.BorderLayout.CENTER);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	/**
	 * Sets a new {@link DefaultListModel} in the excludeVariableList and adds variables conditional on the statement type selected
	 */
	public void populateExcludeVariableList() {
		excludeValues.clear();
		StatementType selected = (StatementType) statementTypeBox.getSelectedItem();
		String[] excludeVariableItems = getVariablesList(selected, true, true, true, true);
		DefaultListModel<String> excludeVariableModel = new DefaultListModel<String>();
		for (int i = 0; i < excludeVariableItems.length - 6; i++) {
			excludeVariableModel.addElement(excludeVariableItems[i]);
			excludeValues.put(excludeVariableItems[i], new ArrayList<String>());
		}
		excludeVariableModel.addElement("author");
		excludeVariableModel.addElement("source");
		excludeVariableModel.addElement("section");
		excludeVariableModel.addElement("type");
		excludeVariableList.setModel(excludeVariableModel);
	}
	
	/**
	 * Show or hide tool tips with instructions depending on whether helpBox is checked
	 */
	public void toggleHelp() {
		javax.swing.ToolTipManager.sharedInstance().setInitialDelay(10);
		if (helpBox.isSelected()) {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(true);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(999999);
		} else {
			javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
			javax.swing.ToolTipManager.sharedInstance().setDismissDelay(0);
		}
	}
	
	/**
	 * Returns a String array with the variables of the {@link StatementType} selected to fill a {@link JComboBox}.
	 * 
	 * @param longtext	boolean indicating whether long text variables should be included.
	 * @param shorttext	boolean indicating whether short text variables should be included.
	 * @param integer	boolean indicating whether integer variables should be included.
	 * @param bool		boolean indicating whether boolean variables should be included.
	 * @return			{@link String[]} with variables of the statementType selected.
	 */
	String[] getVariablesList(StatementType statementType, boolean longtext, boolean shorttext, boolean integer, boolean bool) {
		ArrayList<String> documentVariables = new ArrayList<String>();
		if (shorttext) {
			documentVariables.add("author");
			documentVariables.add("source");
			documentVariables.add("section");
			documentVariables.add("type");
			documentVariables.add("id");
			documentVariables.add("title");
		}
		String[] variables = Stream.concat(
				statementType.getVariables()
				.stream()
				.filter(v -> (shorttext && v.getDataType().equals("short text")) ||
						(longtext && v.getDataType().equals("short text")) ||
						(integer && v.getDataType().equals("integer")) ||
						(bool && v.getDataType().equals("boolean")))
				.map(v -> v.getKey()),
				documentVariables.stream())
				.toArray(String[]::new);
		return variables;
	}
	
	/**
	 * GUI export thread. This is where the computations are executed and the
	 * data are written to a file. 
	 */
	private class GuiExportThread implements Runnable {

		private Exporter exporter;
		private String fileName;
		private ProgressMonitor progressMonitor;
		
		public GuiExportThread(
				String networkType,
				StatementType statementType,
				String variable1,
				boolean variable1Document,
				String variable2,
				boolean variable2Document,
				String qualifier,
				String qualifierAggregation,
				String normalization,
				boolean isolates,
				String duplicates,
				LocalDateTime startDateTime,
				LocalDateTime stopDateTime,
				String timeWindow,
				int windowSize,
				HashMap<String, ArrayList<String>> excludeValues,
				ArrayList<String> excludeAuthors,
				ArrayList<String> excludeSources,
				ArrayList<String> excludeSections,
				ArrayList<String> excludeTypes,
				boolean invertValues,
				boolean invertAuthors,
				boolean invertSources,
				boolean invertSections,
				boolean invertTypes,
				String fileFormat,
				String outfile) {
			this.fileName = outfile;
			this.exporter = new Exporter(
					networkType,
					statementType,
					variable1,
					variable1Document,
					variable2,
					variable2Document,
					qualifier,
					qualifierAggregation,
					normalization,
					isolates,
					duplicates,
					startDateTime,
					stopDateTime,
					timeWindow,
					windowSize,
					excludeValues,
					excludeAuthors,
					excludeSources,
					excludeSections,
					excludeTypes,
					invertValues,
					invertAuthors,
					invertSources,
					invertSections,
					invertTypes,
					fileFormat,
					outfile);
		}
		
		public void run() {
			progressMonitor = new ProgressMonitor(NetworkExporter.this, "Exporting network data.", "(1/4) Filtering statements...", 0, 4);
			progressMonitor.setMillisToDecideToPopup(1);
			// delay the process by a second to make sure the progress monitor shows up
			// see here: https://coderanch.com/t/631127/java/progress-monitor-showing
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			progressMonitor.setProgress(0);
			
			// step 1: load and pre-process data
			progressMonitor.setNote("(1/4) Loading and processing data...");
			exporter.loadData();
			// System.out.println("Export was launched: " + statements.size() + " out of " + Dna.data.getStatements().size() 
			// 		+ " statements retained after filtering.");
			progressMonitor.setProgress(1);
			
			// step 2: filter statements
			progressMonitor.setNote("(2/4) Filtering statements...");
			exporter.filterStatements();
			progressMonitor.setProgress(2);
			
			// step 3: create network data structure
			progressMonitor.setNote("(3/4) Computing network...");
			try {
				exporter.computeResults();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			progressMonitor.setProgress(3);
			
			// step 4: write to file
			progressMonitor.setNote("(4/4) Writing to file...");
			exporter.exportToFile();
			progressMonitor.setProgress(4);
			JOptionPane.showMessageDialog(NetworkExporter.this, "Data were exported to \"" + fileName + "\".");
		}
	}
	
	/**
	 * Combo box renderer for statement types.
	 */
	private class StatementTypeComboBoxRenderer implements ListCellRenderer<Object> {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value == null) {
				JPanel panel = new JPanel();
				JLabel label = new JLabel("");
				panel.add(label);
				return panel;
			} else {
				StatementType statementType = (StatementType) value;
				@SuppressWarnings("serial")
				JButton colorRectangle = (new JButton() {
					public void paintComponent(Graphics g) {
						super.paintComponent(g);
						g.setColor(statementType.getColor());
						g.fillRect(2, 2, 14, 14);
					}
				});
				colorRectangle.setPreferredSize(new Dimension(14, 14));
				colorRectangle.setEnabled(false);
				
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
				panel.add(colorRectangle);
				JLabel statementTypeLabel = new JLabel(statementType.getLabel());
				panel.add(statementTypeLabel);
				
				if (isSelected) {
					UIDefaults defaults = javax.swing.UIManager.getDefaults();
					Color bg = defaults.getColor("List.selectionBackground");
					panel.setBackground(bg);
				}
				
				return panel;
			}
		}
	}

	/**
	 * Backbone dialog for setting penalty and iterations parameters, starting backbone computations, and saving results
	 * to a file.
	 */
	class BackboneDialog extends JDialog {
		public BackboneDialog() {
			this.setModal(true);
			this.setTitle("Backbone");
			ImageIcon backboneIcon = new ImageIcon(getClass().getResource("/icons/tabler-icon-arrows-split.png"));
			this.setIconImage(backboneIcon.getImage());

			JPanel dialogPanel = new JPanel(new BorderLayout());
			CompoundBorder border = BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), new TitledBorder("Find backbone"));
			dialogPanel.setBorder(border);
			this.add(dialogPanel);

			SpinnerNumberModel penaltyModel = new SpinnerNumberModel(7.50, 0.00, 1000.00, 0.01);
			JSpinner penaltySpinner = new JSpinner(penaltyModel);
			((JSpinner.DefaultEditor) penaltySpinner.getEditor()).getTextField().setColumns(4);
			JLabel penaltyLabel = new JLabel("Penalty parameter (p)");
			penaltyLabel.setLabelFor(penaltySpinner);
			String penaltyTooltipText = "<html><p width=\"500\">The penalty (p) determines how large the " +
					"backbone set is, with larger penalties penalizing larger backbones more strongly, hence leading to " +
					"smaller backbones and larger redundant sets. Typical values could be 5.5, 7.5, or 12.</p></html>";
			penaltySpinner.setToolTipText(penaltyTooltipText);
			penaltyLabel.setToolTipText(penaltyTooltipText);

			SpinnerNumberModel iterationsModel = new SpinnerNumberModel(50000, 100, 1000000, 100);
			JSpinner iterationsSpinner = new JSpinner(iterationsModel);
			((JSpinner.DefaultEditor) iterationsSpinner.getEditor()).getTextField().setColumns(7);
			JLabel iterationsLabel = new JLabel("Iterations (T)");
			iterationsLabel.setLabelFor(iterationsSpinner);
			String iterationsTooltipText = "<html><p width=\"500\">The number of iterations (T) determines how long the " +
					"algorithm should run. We have had good results with T = 50,000 iterations for a network of about " +
					"200 actors and about 60 concepts, though fewer iterations might have been acceptable. The quality " +
					"of the solution increases with larger T and reaches the global optimum asymptotically. How many " +
					"iterations are required for a good solution depends on the number of entities on the second-mode " +
					"variable.</p></html>";
			iterationsSpinner.setToolTipText(iterationsTooltipText);
			iterationsLabel.setToolTipText(iterationsTooltipText);

			JButton saveBackboneButton = new JButton("Optimize backbone", new ImageIcon(new ImageIcon(getClass().getResource("/icons/tabler-icon-arrows-split.png")).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH)));
			saveBackboneButton.setToolTipText("<html><p width=\"500\">Find the backbone and redundant set. A custom " +
					"simulated annealing algorithm is employed to find the backbone and redundant sets by minimizing " +
					"penalized Euclidean spectral distances between the backbone network and the full network. The " +
					"results can be saved to JSON or XML files and can inform how to recode entities or which " +
					"entities to include or exclude during network export.</p></html>");
			saveBackboneButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					JFileChooser fc = new JFileChooser();
					fc.removeChoosableFileFilter(fc.getFileFilter());
					fc.addChoosableFileFilter(new FileNameExtensionFilter("JSON and XMl files (*.json, *.xml)", "json","xml"));
					int returnVal = fc.showSaveDialog(getParent());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String fileName = file.getAbsolutePath();
						if (!(fileName.toLowerCase().endsWith(".xml") && !fileName.toLowerCase().endsWith(".json"))) {
							fileName = fileName + ".json";
						}

						// read settings from GUI elements and translate into values for Exporter class
						double penalty = (double) penaltySpinner.getValue();
						int iterations = (int) iterationsSpinner.getValue();
						StatementType statementType = (StatementType) statementTypeBox.getSelectedItem();
						String variable1Name = (String) var1Box.getSelectedItem();
						boolean variable1Document = false;
						if (var1Box.getSelectedIndex() > var1Box.getItemCount() - 7) {
							variable1Document = true;
						}
						String variable2Name = (String) var2Box.getSelectedItem();
						boolean variable2Document = false;
						if (var2Box.getSelectedIndex() > var2Box.getItemCount() - 7) {
							variable2Document = true;
						}
						String qualifier = (String) qualifierBox.getSelectedItem();
						String qualifierAggregation = (String) aggregationBox.getSelectedItem();
						String normalization = (String) normalizationBox.getSelectedItem();
						String duplicates = (String) duplicatesBox.getSelectedItem();
						if (duplicates.equals("include all duplicates")) {
							duplicates = "include";
						} else if (duplicates.equals("ignore per document")) {
							duplicates = "document";
						} else if (duplicates.equals("ignore per calendar week")) {
							duplicates = "week";
						} else if (duplicates.equals("ignore per calendar month")) {
							duplicates = "month";
						} else if (duplicates.equals("ignore per calendar year")) {
							duplicates = "year";
						} else if (duplicates.equals("ignore across date range")) {
							duplicates = "acrossrange";
						}
						LocalDateTime startDateTime = startPicker.getDateTimeStrict();
						LocalDateTime stopDateTime = stopPicker.getDateTimeStrict();

						// start backbone thread
						Thread backboneThread = new Thread(new GuiBackboneThread(penalty, iterations, statementType, variable1Name,
								variable1Document, variable2Name, variable2Document, qualifier, qualifierAggregation,
								normalization, duplicates, startDateTime, stopDateTime,
								NetworkExporter.this.excludeValues, NetworkExporter.this.excludeAuthor,
								NetworkExporter.this.excludeSource, NetworkExporter.this.excludeSection,
								NetworkExporter.this.excludeType, false, false, false,
								false, false, fileName),
								"Compute and export backbone");
						backboneThread.start();
					}
				}
			});

			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();
			dialogPanel.setLayout(gbl);
			gbc.anchor = java.awt.GridBagConstraints.WEST;
			gbc.insets = new java.awt.Insets(10, 10, 10, 10);
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			dialogPanel.add(penaltyLabel, gbc);
			gbc.gridx = 1;
			dialogPanel.add(penaltySpinner, gbc);
			gbc.insets = new java.awt.Insets(3, 10, 10, 10);
			gbc.gridx = 0;
			gbc.gridy = 1;
			dialogPanel.add(iterationsLabel, gbc);
			gbc.gridx = 1;
			dialogPanel.add(iterationsSpinner, gbc);
			gbc.gridx = 0;
			gbc.gridwidth = 2;
			gbc.gridy = 2;
			dialogPanel.add(saveBackboneButton, gbc);

			this.pack();
			this.setLocationRelativeTo(null);
			this.setVisible(true);
		}
	}

	/**
	 * GUI backbone thread. This is where the computations are executed and the
	 * data are written to a file.
	 */
	private class GuiBackboneThread implements Runnable {

		private Exporter exporter;
		private String fileName;
		private double p;
		private int T;

		public GuiBackboneThread(
				double p,
				int T,
				StatementType statementType,
				String variable1,
				boolean variable1Document,
				String variable2,
				boolean variable2Document,
				String qualifier,
				String qualifierAggregation,
				String normalization,
				String duplicates,
				LocalDateTime startDateTime,
				LocalDateTime stopDateTime,
				HashMap<String, ArrayList<String>> excludeValues,
				ArrayList<String> excludeAuthors,
				ArrayList<String> excludeSources,
				ArrayList<String> excludeSections,
				ArrayList<String> excludeTypes,
				boolean invertValues,
				boolean invertAuthors,
				boolean invertSources,
				boolean invertSections,
				boolean invertTypes,
				String outfile) {
			this.p = p;
			this.T = T;
			this.fileName = outfile;
			this.exporter = new Exporter(
					"onemode",
					statementType,
					variable1,
					variable1Document,
					variable2,
					variable2Document,
					qualifier,
					qualifierAggregation,
					normalization,
					true,
					duplicates,
					startDateTime,
					stopDateTime,
					"no",
					1,
					excludeValues,
					excludeAuthors,
					excludeSources,
					excludeSections,
					excludeTypes,
					invertValues,
					invertAuthors,
					invertSources,
					invertSections,
					invertTypes,
					"",
					"");
		}

		public void run() {
			this.exporter.backbone(p, T);
			try {
				this.exporter.writeBackboneToFile(fileName);
				JOptionPane.showMessageDialog(NetworkExporter.this, "Data were exported to \"" + fileName + "\".");
			} catch (IOException e) {
				LogEvent l = new LogEvent(Logger.ERROR,
						"Backbone results could not be saved.",
						"Tried to save backbone results to a file, but an error occurred.",
						e);
				Dna.logger.log(l);
			}
		}
	}
}