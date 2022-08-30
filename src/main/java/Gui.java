import net.sf.jsqlparser.statement.select.SelectItem;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GraphicsDevice.WindowTranslucency.*;

public class Gui extends JFrame {
    Pg pg = new Pg();
    Common common = new Common();
    private int guiWindowX;
    private int guiWindowY;
    // icons
    AtomicBoolean isGuiInTray = new AtomicBoolean(false);
    ImageIcon logoIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(this.getClass().getResource("icons/logo.png")));
    ImageIcon configIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/config.png")));
    ImageIcon configIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/config2.png")));
    ImageIcon connectIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/connect.png")));
    ImageIcon connectIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/connect2.png")));
    ImageIcon connectIcon3 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/connect3.png")));
    ImageIcon refreshIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/refresh.png")));
    ImageIcon refreshIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/refresh2.png")));
    ImageIcon exitIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/exit.png")));
    ImageIcon exitIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/exit2.png")));
    ImageIcon munusIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/minus.png")));
    ImageIcon munusIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("icons/minus2.png")));
    // Split panel, Left table
    static JTable executeTable;
    static DefaultTableModel executeModel;
    private final TableColumnModel executeColumnModel;
    private TableColumnModel selectColumnModel;
    // Right table
    static JTable selectTable;
    static DefaultTableModel selectModel;
    int headerX;
    int headerY;
    Checkbox tableCheckbox;
    Checkbox viewCheckbox;
    JTextArea textArea;
    JButton connectionBtn;
    SystemTray systemTray;
    static String definitionFromMatview;

    public Gui() {
        // ширина дисплея
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double systemScreenWidth = screenSize.getWidth();
        // Определяем количество мониторов, чтобы разместить приложение на втором, если он есть
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        if (gs.length == 1) {
            if (systemScreenWidth == 1920.0) {
                guiWindowX = 250;
                guiWindowY = 100;
            } else {
                guiWindowX = 1;
                guiWindowY = 1;
            }
        } else if (gs.length == 2) {
            if (systemScreenWidth == 1920.0) {
                guiWindowX = 2230;
                guiWindowY = 100;
            } else {
                guiWindowX = 1981;
                guiWindowY = 1;
            }
        }

        this.setTitle("mrpro-pgsql-ide");
        this.setResizable(false);
        this.setIconImage(logoIcon.getImage());
        this.setFont(new Font("Tahoma", Font.PLAIN, 14));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        int guiWindowHeight = 860;
        int guiWindowWidth = 1390;
        this.setBounds(guiWindowX, guiWindowY, guiWindowWidth, guiWindowHeight);
        this.getContentPane().setBackground(new Color(238, 248, 254));
        this.getContentPane().setLayout(null);
        //this.setAlwaysOnTop(true);

        // Прозрачность и оформление окна
        this.setUndecorated(true);
        // Проверка поддерживает ли операционная система прозрачность окон
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isUniformTranslucencySupported = gd.isWindowTranslucencySupported(TRANSLUCENT);
        if (isUniformTranslucencySupported) this.setOpacity(pg.guiOpacity);

        // Открыть файл config.txt
        JButton editConfig = new JButton(configIcon);
        editConfig.setToolTipText("Settings");
        editConfig.setBackground(new Color(255, 255, 255));
        editConfig.setFont(new Font("Tahoma", Font.BOLD, 10));
        editConfig.setBounds(5, 5, 24, 20);
        editConfig.setContentAreaFilled(false);
        editConfig.setBorderPainted(false);
        editConfig.setFocusable(false);
        getContentPane().add(editConfig);
        editConfig.addActionListener(e -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                try {
                    Desktop.getDesktop().open(new File(Main.configPath));
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });

        // изменение иконки
        editConfig.addMouseListener(new MouseAdapter() {
            // наведение на иконку
            public void mouseEntered(MouseEvent e) {
                if (editConfig.getIcon() == configIcon) {
                    editConfig.setIcon(configIcon2);
                }
            }

            // убрали мышку с иконки
            public void mouseExited(MouseEvent e) {
                if ((editConfig.getIcon() == configIcon2)) {
                    editConfig.setIcon(configIcon);
                }

            }
        });

        // Connection button
        connectionBtn = new JButton(connectIcon);
        connectionBtn.setToolTipText("Connect");
        connectionBtn.setBackground(new Color(255, 255, 255));
        connectionBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
        connectionBtn.setBounds(32, 5, 24, 20);
        connectionBtn.setContentAreaFilled(false);
        connectionBtn.setBorderPainted(false);
        connectionBtn.setFocusable(false);
        getContentPane().add(connectionBtn);
        //Listener
        connectionBtn.addActionListener((e) -> {
            Pg pg = new Pg();
            if (!Pg.isConnect) pg.connect();
            else {
                connectionBtn.setIcon(connectIcon);
                if (executeModel.getRowCount() > 0) executeModel.setRowCount(0);
                if (selectModel != null && selectModel.getRowCount() > 0) selectModel.setRowCount(0);
                textArea.setEnabled(false);
                tableCheckbox.setEnabled(false);
                viewCheckbox.setEnabled(false);
                pg.close();
            }
            if (Pg.isConnect) {
                textArea.setEnabled(true);
                tableCheckbox.setEnabled(true);
                viewCheckbox.setEnabled(true);
                tableCheckbox.setState(true);
                viewCheckbox.setState(true);
                connectionBtn.setIcon(connectIcon3);
            }
        });
        // изменение иконки
        connectionBtn.addMouseListener(new MouseAdapter() {
            // наведение на иконку
            public void mouseEntered(MouseEvent e) {
                if (connectionBtn.getIcon() == connectIcon) {
                    connectionBtn.setIcon(connectIcon2);
                }
            }

            // убрали мышку с иконки
            public void mouseExited(MouseEvent e) {
                if ((connectionBtn.getIcon() == connectIcon2)) {
                    connectionBtn.setIcon(connectIcon);
                }
            }
        });

        // Exit button
        JButton exitBtn = new JButton(exitIcon);
        exitBtn.setBackground(new Color(255, 255, 255));
        exitBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
        exitBtn.setBounds(1363, 5, 24, 20);
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setFocusable(false);
        getContentPane().add(exitBtn);
        //Listener
        exitBtn.addActionListener((e) -> {
            if (Pg.isConnect) {
                pg.close();
            }
            System.exit(0);
        });
        // изменение иконки
        exitBtn.addMouseListener(new MouseAdapter() {
            // наведение на иконку
            public void mouseEntered(MouseEvent e) {
                if (exitBtn.getIcon() == exitIcon) {
                    exitBtn.setIcon(exitIcon2);
                }
            }

            // убрали мышку с иконки
            public void mouseExited(MouseEvent e) {
                if ((exitBtn.getIcon() == exitIcon2)) {
                    exitBtn.setIcon(exitIcon);
                }
            }
        });

        // Сворачивание в трей
        JButton toTrayBtn = new JButton(munusIcon);
        toTrayBtn.setFont(new Font("Tahoma", Font.BOLD, 11));
        toTrayBtn.setFocusable(false);
        toTrayBtn.setContentAreaFilled(false);
        toTrayBtn.setBorderPainted(false);
        toTrayBtn.setBackground(Color.WHITE);
        toTrayBtn.setBounds(1335, 5, 24, 20);
        if (SystemTray.isSupported()) {
            getContentPane().add(toTrayBtn);
        }
        toTrayBtn.addActionListener(e -> {
            setVisible(false);
            isGuiInTray.set(true);
        });
        // изменение иконки
        toTrayBtn.addMouseListener(new MouseAdapter() {
            // наведение на иконку
            public void mouseEntered(MouseEvent e) {
                if (toTrayBtn.getIcon() == munusIcon) {
                    toTrayBtn.setIcon(munusIcon2);
                }
            }

            // убрали мышку с иконки
            public void mouseExited(MouseEvent e) {
                if ((toTrayBtn.getIcon() == munusIcon2)) {
                    toTrayBtn.setIcon(munusIcon);
                }
            }
        });

        //split panel
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBounds(5, 30, 1380, 820);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(pg.dividerVertical);
        getContentPane().add(splitPane);
        //splitPane.addPropertyChangeListener(e -> pg.dividerVertical = splitPane.getDividerLocation());
        JScrollPane leftPanel = new JScrollPane();
        splitPane.setLeftComponent(leftPanel);
        JScrollPane rightPanelBottom = new JScrollPane();
        JScrollPane rightPanelTop = new JScrollPane();
        JSplitPane splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

        splitVertical.setTopComponent(rightPanelTop);
        splitVertical.setBottomComponent(rightPanelBottom);

        splitPane.setRightComponent(splitVertical);
        splitVertical.setBottomComponent(rightPanelBottom);
        splitVertical.setDividerLocation(pg.dividerHorizontal);
        splitPane.setRightComponent(splitVertical);

        // Refresh button
        JButton refreshBtn = new JButton(refreshIcon);
        refreshBtn.setToolTipText("Refresh");
        refreshBtn.setBackground(new Color(255, 255, 255));
        refreshBtn.setFont(new Font("Tahoma", Font.BOLD, 10));
        refreshBtn.setBounds(60, 5, 24, 20);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusable(false);
        getContentPane().add(refreshBtn);
        refreshBtn.addActionListener(e -> {
            // удалить старый frame
            this.dispose();
            // удаление иконки в трее
            TrayIcon[] trayIcons = systemTray.getTrayIcons();
            pg.close();
            for (TrayIcon t : trayIcons) {
                systemTray.remove(t);
            }
            //создание нового frame
            Runnable runnable = () -> {
                final Gui newGui = new Gui();
                FrameDragListener frameDragListener = new FrameDragListener(newGui);
                newGui.addMouseListener(frameDragListener);
                newGui.addMouseMotionListener(frameDragListener);
            };
            SwingUtilities.invokeLater(runnable);
        });
        //
        refreshBtn.addMouseListener(new MouseAdapter() {
            // наведение на иконку
            public void mouseEntered(MouseEvent e) {
                if (refreshBtn.getIcon() == refreshIcon) {
                    refreshBtn.setIcon(refreshIcon2);
                }
            }

            // убрали мышку с иконки
            public void mouseExited(MouseEvent e) {
                if ((refreshBtn.getIcon() == refreshIcon2)) {
                    refreshBtn.setIcon(refreshIcon);
                }

            }
        });

        //My sign
        JLabel labelSign = new JLabel(":mrprogre");
        labelSign.setForeground(new Color(25, 10, 122));
        labelSign.setEnabled(false);
        labelSign.setFont(new Font("Tahoma", Font.BOLD, 12));
        labelSign.setBounds(1269, 7, 70, 14);
        getContentPane().add(labelSign);

        labelSign.addMouseListener(new MouseAdapter() {
            // наведение мышки на письмо
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!labelSign.isEnabled()) {
                    labelSign.setEnabled(true);
                }
            }

            // убрали мышку с письма
            @Override
            public void mouseExited(MouseEvent e) {
                if (labelSign.isEnabled()) {
                    labelSign.setEnabled(false);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    String url = "https://github.com/mrprogre";
                    URI uri = null;
                    try {
                        uri = new URI(url);
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                    Desktop desktop = Desktop.getDesktop();
                    assert uri != null;
                    try {
                        desktop.browse(uri);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Список таблиц пользователя
        //Object[] executeColumns = {"Num", "Fav", "Type", "Name", "Σ", "Rows", "Comments", " "};
        Object[] executeColumns = {"Num", "Fav", "Type", "Name", "Comments", " ", "Rows", "Σ"};
        executeModel = new DefaultTableModel(new Object[][]{
        }, executeColumns) {
            final boolean[] columnEditables = new boolean[]{false, true, false, false, true, true, false, true};

            public boolean isCellEditable(int row, int column) {
                return this.columnEditables[column];
            }

            final Class[] types = {Integer.class, Boolean.class, String.class, String.class, String.class, Button.class, String.class, Button.class};

            @Override
            public Class getColumnClass(int columnIndex) {
                return this.types[columnIndex];
            }
        };
        executeTable = new JTable(executeModel) {
            // tooltips
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int column = 4;
                try {
                    tip = (String) getValueAt(rowIndex, column);
                } catch (RuntimeException ignored) {
                }
                assert tip != null;
                if (executeModel.getRowCount() > 0 && tip.length() > 6) {
                    return tip;
                } else return null;
            }
        };
        executeTable.getColumn("Σ").setCellRenderer(new rowsCountBtn(executeTable, 7));
        executeTable.getColumn(" ").setCellRenderer(new saveInfoBtn(executeTable, 5));
        executeTable.setDefaultRenderer(String.class, new TableInfoRenderer());
        executeTable.setAutoCreateRowSorter(true);
        executeColumnModel = executeTable.getColumnModel();
        // cell border color
        executeTable.setGridColor(new Color(58, 79, 79));
        //executeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // table background color
        executeTable.setFillsViewportHeight(true);
        executeTable.setCellSelectionEnabled(true);
        executeTable.setBackground(new Color(250, 252, 255));
        // headers settings
        JTableHeader header = executeTable.getTableHeader();
        SimpleHeaderRenderer headerRenderer = new SimpleHeaderRenderer();
        header.setDefaultRenderer(headerRenderer);
        header.setDefaultRenderer(new KeepSortIconHeaderRenderer(header.getDefaultRenderer()));
        // высота заголовка
        executeTable.getTableHeader().setPreferredSize(
                new Dimension(leftPanel.getHeight(), 26)
        );
        executeTable.getTableHeader().setReorderingAllowed(false);
        header.setFont(new Font("Tahoma", Font.BOLD, 13));
        //sorter
        RowSorter<TableModel> sorter = new TableRowSorter<>(executeModel);
        executeTable.setRowSorter(sorter);
        //
        DefaultTableCellRenderer Renderer = new DefaultTableCellRenderer();
        Renderer.setHorizontalAlignment(JLabel.CENTER);
        executeTable.getColumn("Num").setCellRenderer(Renderer);
        executeTable.getColumn("Num").setMinWidth(30);
        executeTable.getColumn("Num").setPreferredWidth(40);
        executeTable.getColumn("Type").setCellRenderer(Renderer);
        executeTable.getColumn("Name").setPreferredWidth(290);
        executeTable.getColumn("Fav").setMinWidth(30);
        executeTable.getColumn("Fav").setPreferredWidth(40);
        executeTable.getColumn("Rows").setPreferredWidth(120);
        executeTable.getColumn("Rows").setCellRenderer(Renderer);
        executeTable.getColumn("Σ").setMaxWidth(30);
        executeTable.getColumn("Comments").setPreferredWidth(120);
        executeTable.getColumn(" ").setMaxWidth(30);
        executeTable.setRowHeight(22);
        executeTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        executeTable.setSelectionBackground(new Color(0, 0, 0, 42));
        //
        leftPanel.setViewportView(executeTable);

        // Клики в левой таблице
        executeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int columnIdx = executeColumnModel.getColumnIndexAtX(e.getX());
                if (columnIdx == 1) {
                    String[] favTables = common.getFavoriteFromFile();
                    int rowIdx = executeTable.getSelectedRow();
                    if (executeModel.getRowCount() > 0) {
                        String favTable = executeTable.getValueAt(rowIdx, 3).toString();
                        boolean inFavorites = Arrays.asList(favTables).contains(favTable);
                        if (e.getClickCount() == 1 && !inFavorites) {
                            common.writeToConfig("favorite_table", favTable);
                        } else if (e.getClickCount() == 1 && inFavorites) {
                            common.deleteFromFavorites("favorite_table", favTable);
                        }
                    }
                }

                if (e.getClickCount() == 2 && executeTable.getSelectedColumn() == 3) {
                    if (executeModel.getRowCount() > 0) {
                        int row = executeTable.getSelectedRow();
                        String tableName = executeTable.getValueAt(row, 3).toString();
                        pg.getUserColumns(tableName);
                        String objectType = executeTable.getValueAt(row, 2).toString();
                        // Получаем название таблицы на которой построен matview
                        if (objectType.equals("matview")) {
                            String matViewDefinition = "SELECT definition FROM pg_matviews WHERE matviewname = '" + tableName + "' ORDER BY schemaname, matviewname";
                            try {
                                definitionFromMatview = null;
                                PreparedStatement mv = Pg.connection.prepareStatement(matViewDefinition);
                                ResultSet rsMv = mv.executeQuery();
                                while (rsMv.next()) {
                                    definitionFromMatview = rsMv.getString(1)
                                            .substring(1)
                                            .replaceAll("\n", " ")
                                            .replaceAll(" {5}", " ")
                                            .replaceAll(" {4}", " ")
                                            .replaceAll(";", "")
                                            .toLowerCase(Locale.ROOT);
                                }
                                mv.close();
                                rsMv.close();
                                assert definitionFromMatview != null;
                                tableName = definitionFromMatview.substring(definitionFromMatview.indexOf("from") + 5);
                                pg.getUserColumns(tableName);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }

                        Object[] selectTableColumns = pg.userColumns.toArray();
                        selectModel = new DefaultTableModel(new Object[][]{
                        }, selectTableColumns) {
                            // Сортировка в любой таблице по любому типу столбца
                            final Class[] types = common.typeClass(pg.types);

                            @Override
                            public Class getColumnClass(int columnIndex) {
                                return this.types[columnIndex];
                            }

                            final boolean[] columnEditables = new boolean[pg.types.size()];

                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return this.columnEditables[column];
                            }
                        };
                        selectTable = new JTable(selectModel);
                        selectColumnModel = selectTable.getColumnModel();
                        selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                        selectTable.setAutoCreateRowSorter(true);
                        //headers
                        JTableHeader selectHeader = selectTable.getTableHeader();
                        selectTable.getTableHeader().setReorderingAllowed(false);
                        selectHeader.setFont(new Font("Tahoma", Font.BOLD, 13));
                        // высота заголовка
                        selectTable.getTableHeader().setPreferredSize(
                                new Dimension(rightPanelTop.getHeight(), 26)
                        );
                        selectTable.setRowHeight(20);
                        selectTable.setDefaultRenderer(Object.class, new SelectTableInfoRenderer());
                        selectTable.setDefaultRenderer(Date.class, new SelectTableInfoRenderer());
                        selectTable.setDefaultRenderer(Number.class, new SelectTableInfoRenderer());
                        selectTable.setDefaultRenderer(Boolean.class, new SelectTableInfoRenderer());
                        selectTable.setColumnSelectionAllowed(true);
                        selectTable.setCellSelectionEnabled(true);
                        selectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        selectTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
                        // Colors
                        selectTable.setForeground(Color.black);
                        selectTable.setSelectionForeground(new Color(26, 79, 164));
                        selectTable.setSelectionBackground(new Color(255, 255, 160));
                        // ширина всех столбцов
                        for (int i = 0; i < pg.userColumns.size(); i++) {
                            switch (pg.types.get(i)) {
                                case "int4":
                                case "int8":
                                    selectTable.getColumnModel().getColumn(i).setPreferredWidth(60);
                                    break;
                                case "date":
                                case "timestamp":
                                    selectTable.getColumnModel().getColumn(i).setPreferredWidth(130);
                                    break;
                            }
                        }
                        rightPanelTop.setViewportView(selectTable);
                        //
                        if (selectModel.getColumnCount() > 0) selectModel.setRowCount(0);
                        pg.selectFromTable(tableName, objectType);

                        selectTable.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                // двойным кликом выделить всю строку
                                if (e.getClickCount() == 2) {
                                    selectTable.setColumnSelectionInterval(0, selectTable.getColumnCount() - 1);
                                }

                                // правая кнопка - контекстное меню
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    final JPopupMenu popup = new JPopupMenu();
                                    // copy (menu)
                                    JMenuItem menuCopy = new JMenuItem("Copy");
                                    menuCopy.addActionListener((e2) -> {
                                        StringBuilder sbf = new StringBuilder();
                                        int numCols = selectTable.getSelectedColumnCount();
                                        int numRows = selectTable.getSelectedRowCount();
                                        int[] rowsSelected = selectTable.getSelectedRows();
                                        int[] colsSelected = selectTable.getSelectedColumns();
                                        for (int i = 0; i < numRows; ++i) {
                                            for (int j = 0; j < numCols; ++j) {
                                                sbf.append(selectTable.getValueAt(rowsSelected[i], colsSelected[j]));
                                                if (j < numCols - 1) {
                                                    sbf.append(" ");
                                                }
                                            }
                                            sbf.append("\n");
                                        }
                                        StringSelection stsel = new StringSelection(sbf.toString());
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(stsel, stsel);
                                    });
                                    popup.add(menuCopy);

                                    // Delete rows (menu)
                                    JMenuItem menuDeleteRow = new JMenuItem("Delete");
                                    menuDeleteRow.addActionListener((e2) -> {
                                        int numRows = selectTable.getSelectedRowCount();

                                        if (numRows > 1) {
                                            int[] rows = selectTable.getSelectedRows();
                                            for (int i = rows.length - 1; i >= 0; --i) {
                                                selectModel.removeRow(rows[i]);
                                            }
                                        } else {
                                            int row = selectTable.convertRowIndexToModel(selectTable.rowAtPoint(e.getPoint()));
                                            selectModel.removeRow(row);
                                        }
                                    });
                                    popup.add(menuDeleteRow);

                                    popup.show(selectTable, e.getX(), e.getY());
                                }
                            }
                        });

                        // popup menu
                        JPopupMenu selectHeadersMenu = new JPopupMenu();

                        selectHeader.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                if (SwingUtilities.isRightMouseButton(e)) {
                                    headerX = e.getX();
                                    headerY = e.getY();
                                    selectHeadersMenu.show(selectHeader, headerX, headerY);
                                }
                            }
                        });
                        // 1 Copy header
                        JMenuItem itemCopyHeader = new JMenuItem("Copy header");
                        itemCopyHeader.setBackground(new Color(255, 240, 246));
                        itemCopyHeader.addActionListener(e1 -> {
                            String header = selectTable.getColumnName(selectColumnModel.getColumnIndexAtX(headerX));
                            StringSelection stringSelection = new StringSelection(header);
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        });
                        selectHeadersMenu.add(itemCopyHeader);
                        // 2 Copy headers
                        JMenuItem itemCopyHeaders = new JMenuItem("Copy all headers");
                        itemCopyHeaders.setBackground(new Color(255, 254, 240));
                        itemCopyHeaders.addActionListener(e2 -> {
                            Enumeration<TableColumn> cols = selectColumnModel.getColumns();
                            StringBuilder columns = new StringBuilder();
                            while (cols.hasMoreElements()) {
                                TableColumn column = cols.nextElement();
                                columns.append(column.getHeaderValue()).append(", ");
                            }
                            StringSelection stringSelection = new StringSelection(columns.substring(0, columns.length() - 2));
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        });
                        selectHeadersMenu.add(itemCopyHeaders);
                        // 3 Copy data as row
                        JMenuItem itemCopydataRows = new JMenuItem("Copy data as row");
                        itemCopydataRows.setBackground(new Color(240, 251, 255));
                        itemCopydataRows.addActionListener(e3 -> {
                            selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                            int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                            StringBuilder rows = new StringBuilder();
                            for (int i = 0; i < selectTable.getRowCount(); i++) {
                                rows.append(selectTable.getValueAt(i, columnIndex)).append(", ");
                            }
                            StringSelection stringSelection = new StringSelection(rows.substring(0, rows.length() - 2));
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        });
                        selectHeadersMenu.add(itemCopydataRows);
                        // 4 Copy data as column
                        JMenuItem itemCopydataColumns = new JMenuItem("Copy data as column");
                        itemCopydataColumns.setBackground(new Color(241, 253, 239));
                        itemCopydataColumns.addActionListener(e4 -> {
                            selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                            int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                            StringBuilder rows = new StringBuilder();
                            for (int i = 0; i < selectTable.getRowCount(); i++) {
                                rows.append(selectTable.getValueAt(i, columnIndex)).append("\n");
                            }
                            StringSelection stringSelection = new StringSelection(rows.toString());
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);

                        });
                        selectHeadersMenu.add(itemCopydataColumns);
                        // 5 Copy data as expression
                        JMenuItem itemCopydataAsExptression = new JMenuItem("Copy data as expression");
                        itemCopydataAsExptression.setBackground(new Color(255, 243, 232));
                        itemCopydataAsExptression.addActionListener(e5 -> {
                            //selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                            int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                            StringBuilder rows = new StringBuilder();
                            rows.append("in (");
                            for (int i = 0; i < selectTable.getRowCount(); i++) {
                                rows.append("'").append(selectTable.getValueAt(i, columnIndex)).append("', ");
                            }
                            StringSelection stringSelection = new StringSelection(rows.substring(0, rows.length() - 2) + ");");
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        });
                        selectHeadersMenu.add(itemCopydataAsExptression);
                    }
                }
            }
        });

        // popup menu
        JPopupMenu executeHeadersMenu = new JPopupMenu();
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    headerX = e.getX();
                    headerY = e.getY();
                    executeHeadersMenu.show(header, headerX, headerY);
                }
            }
        });

        // 1 Copy header
        JMenuItem itemHeader = new JMenuItem("Copy header");
        itemHeader.setBackground(new Color(255, 240, 246));
        itemHeader.addActionListener(e -> {
            String tableName = executeTable.getColumnName(executeColumnModel.getColumnIndexAtX(headerX));
            StringSelection stringSelection = new StringSelection(tableName);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        executeHeadersMenu.add(itemHeader);
        // 2 Copy headers
        JMenuItem itemShow = new JMenuItem("Copy headers");
        itemShow.setBackground(new Color(255, 254, 240));
        itemShow.addActionListener(e -> {
            Enumeration<TableColumn> cols = executeColumnModel.getColumns();
            StringBuilder columns = new StringBuilder();

            while (cols.hasMoreElements()) {
                TableColumn column = cols.nextElement();
                columns.append(column.getHeaderValue()).append(", ");
            }
            StringSelection stringSelection = new StringSelection(columns.substring(0, columns.length() - 2));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        executeHeadersMenu.add(itemShow);
        // 3 Copy headers as column
        JMenuItem itemClose = new JMenuItem("Copy data as column");
        itemClose.setBackground(new Color(240, 251, 255));
        itemClose.addActionListener(e -> {
            executeTable.setRowSelectionInterval(0, executeTable.getRowCount() - 1);
            int columnIndex = executeColumnModel.getColumnIndexAtX(headerX);

            StringBuilder rows = new StringBuilder();
            for (int i = 0; i < executeTable.getRowCount(); i++) {
                rows.append(executeTable.getValueAt(i, columnIndex)).append("\n");
            }
            StringSelection stringSelection = new StringSelection(rows.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        executeHeadersMenu.add(itemClose);

        // Текстовая область
        textArea = new JTextArea("select * from ");
        textArea.setEnabled(false);
        textArea.setFont(new Font("Tahoma", Font.BOLD, 13));
        textArea.setLineWrap(true);
        textArea.setBackground(new Color(255, 243, 243));
        rightPanelBottom.setViewportView(textArea);
        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 120) { // F9
                    // Определяем название таблицы
                    List<String> allTablesFromQuery = common.getTableName(textArea.getText());
                    String tableName = allTablesFromQuery.get(0);
                    List<SelectItem> allColumnsFromQuery = common.getColumns(textArea.getText());

                    StringBuilder columnsForQuery = new StringBuilder();
                    for (SelectItem selectItem : allColumnsFromQuery) {
                        columnsForQuery
                                .append("'")
                                .append(selectItem)
                                .append("', ");
                    }
                    columnsForQuery = new StringBuilder(columnsForQuery.substring(0, columnsForQuery.length() - 2));
                    pg.getUserColumns(tableName, columnsForQuery.toString());

                    Object[] selectTableColumns = pg.userColumns.toArray();
                    selectModel = new DefaultTableModel(new Object[][]{
                    }, selectTableColumns) {
                        // Сортировка в любой таблице по любому типу столбца
                        final Class[] types = common.typeClass(pg.types);
                        @Override
                        public Class getColumnClass(int columnIndex) {
                            return this.types[columnIndex];
                        }

                        final boolean[] columnEditables = new boolean[pg.types.size()];

                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return this.columnEditables[column];
                        }
                    };
                    selectTable = new JTable(selectModel);
                    selectTable.setDefaultRenderer(Object.class, new SelectTableInfoRenderer());
                    selectTable.setDefaultRenderer(Date.class, new SelectTableInfoRenderer());
                    selectTable.setDefaultRenderer(Number.class, new SelectTableInfoRenderer());
                    selectTable.setDefaultRenderer(Boolean.class, new SelectTableInfoRenderer());
                    selectColumnModel = selectTable.getColumnModel();
                    selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    selectTable.setAutoCreateRowSorter(true);
                    //headers
                    JTableHeader selectHeader = selectTable.getTableHeader();
                    selectTable.getTableHeader().setReorderingAllowed(false);
                    selectHeader.setFont(new Font("Tahoma", Font.BOLD, 13));
                    // высота заголовка
                    selectTable.getTableHeader().setPreferredSize(
                            new Dimension(rightPanelTop.getHeight(), 26)
                    );
                    //Cell alignment
                    DefaultTableCellRenderer executeRenderer = new DefaultTableCellRenderer();
                    executeRenderer.setHorizontalAlignment(JLabel.CENTER);
                    selectTable.setRowHeight(20);
                    selectTable.setColumnSelectionAllowed(true);
                    selectTable.setCellSelectionEnabled(true);
                    selectTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    selectTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    // Colors
                    selectTable.setForeground(Color.black);
                    selectTable.setSelectionForeground(new Color(26, 79, 164));
                    selectTable.setSelectionBackground(new Color(255, 255, 160));
                    // ширина всех столбцов
                    for (int i = 0; i < pg.userColumns.size(); i++) {
                        switch (pg.types.get(i)) {
                            case "int4":
                            case "int8":
                                selectTable.getColumnModel().getColumn(i).setPreferredWidth(60);
                                break;
                            case "date":
                            case "timestamp":
                                selectTable.getColumnModel().getColumn(i).setPreferredWidth(130);
                                break;
                        }
                    }
                    rightPanelTop.setViewportView(selectTable);
                    //
                    if (selectModel.getColumnCount() > 0) selectModel.setRowCount(0);
                    //pg.selectFromTable(tableName, "table"); // надо ли? убрал

                    selectTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // двойным кликом выделить всю строку
                            if (e.getClickCount() == 2) {
                                selectTable.setColumnSelectionInterval(0, selectTable.getColumnCount() - 1);
                            }

                            // правая кнопка - контекстное меню
                            if (SwingUtilities.isRightMouseButton(e)) {
                                final JPopupMenu popup = new JPopupMenu();
                                // copy (menu)
                                JMenuItem menuCopy = new JMenuItem("Copy");
                                menuCopy.addActionListener((e2) -> {
                                    StringBuilder sbf = new StringBuilder();
                                    int numCols = selectTable.getSelectedColumnCount();
                                    int numRows = selectTable.getSelectedRowCount();
                                    int[] rowsSelected = selectTable.getSelectedRows();
                                    int[] colsSelected = selectTable.getSelectedColumns();
                                    for (int i = 0; i < numRows; ++i) {
                                        for (int j = 0; j < numCols; ++j) {
                                            sbf.append(selectTable.getValueAt(rowsSelected[i], colsSelected[j]));
                                            if (j < numCols - 1) {
                                                sbf.append(" ");
                                            }
                                        }
                                        sbf.append("\n");
                                    }
                                    StringSelection stsel = new StringSelection(sbf.toString());
                                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    clipboard.setContents(stsel, stsel);
                                });
                                popup.add(menuCopy);

                                // Delete rows (menu)
                                JMenuItem menuDeleteRow = new JMenuItem("Delete");
                                menuDeleteRow.addActionListener((e2) -> {
                                    int numRows = selectTable.getSelectedRowCount();

                                    if (numRows > 1) {
                                        int[] rows = selectTable.getSelectedRows();
                                        for (int i = rows.length - 1; i >= 0; --i) {
                                            selectModel.removeRow(rows[i]);
                                        }
                                    } else {
                                        int row = selectTable.convertRowIndexToModel(selectTable.rowAtPoint(e.getPoint()));
                                        selectModel.removeRow(row);
                                    }
                                });
                                popup.add(menuDeleteRow);

                                popup.show(selectTable, e.getX(), e.getY());
                            }
                        }
                    });


                    // popup menu
                    JPopupMenu selectHeadersMenu = new JPopupMenu();

                    selectHeader.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                headerX = e.getX();
                                headerY = e.getY();
                                selectHeadersMenu.show(selectHeader, headerX, headerY);
                            }
                        }
                    });
                    // 1 Copy header
                    JMenuItem itemCopyHeader = new JMenuItem("Copy header");
                    itemCopyHeader.setBackground(new Color(255, 240, 246));
                    itemCopyHeader.addActionListener(e1 -> {
                        String header = selectTable.getColumnName(selectColumnModel.getColumnIndexAtX(headerX));
                        StringSelection stringSelection = new StringSelection(header);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                    });
                    selectHeadersMenu.add(itemCopyHeader);
                    // 2 Copy headers
                    JMenuItem itemCopyHeaders = new JMenuItem("Copy all headers");
                    itemCopyHeaders.setBackground(new Color(255, 254, 240));
                    itemCopyHeaders.addActionListener(e2 -> {
                        Enumeration<TableColumn> cols = selectColumnModel.getColumns();
                        StringBuilder columns = new StringBuilder();
                        while (cols.hasMoreElements()) {
                            TableColumn column = cols.nextElement();
                            columns.append(column.getHeaderValue()).append(", ");
                        }
                        StringSelection stringSelection = new StringSelection(columns.substring(0, columns.length() - 2));
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                    });
                    selectHeadersMenu.add(itemCopyHeaders);
                    // 3 Copy data as row
                    JMenuItem itemCopydataRows = new JMenuItem("Copy data as row");
                    itemCopydataRows.setBackground(new Color(240, 251, 255));
                    itemCopydataRows.addActionListener(e3 -> {
                        selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                        int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                        StringBuilder rows = new StringBuilder();
                        for (int i = 0; i < selectTable.getRowCount(); i++) {
                            rows.append(selectTable.getValueAt(i, columnIndex)).append(", ");
                        }
                        StringSelection stringSelection = new StringSelection(rows.substring(0, rows.length() - 2));
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);
                    });
                    selectHeadersMenu.add(itemCopydataRows);
                    // 4 Copy data as column
                    JMenuItem itemCopydataColumns = new JMenuItem("Copy data as column");
                    itemCopydataColumns.setBackground(new Color(241, 253, 239));
                    itemCopydataColumns.addActionListener(e4 -> {
                        selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                        int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                        StringBuilder rows = new StringBuilder();
                        for (int i = 0; i < selectTable.getRowCount(); i++) {
                            rows.append(selectTable.getValueAt(i, columnIndex)).append("\n");
                        }
                        StringSelection stringSelection = new StringSelection(rows.toString());
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);

                    });
                    selectHeadersMenu.add(itemCopydataColumns);
                    // 5 Copy data as expression
                    JMenuItem itemCopydataAsExptression = new JMenuItem("Copy data as expression");
                    itemCopydataAsExptression.setBackground(new Color(255, 243, 232));
                    itemCopydataAsExptression.addActionListener(e5 -> {
                        //selectTable.setRowSelectionInterval(0, selectTable.getRowCount() - 1);
                        int columnIndex = selectColumnModel.getColumnIndexAtX(headerX);

                        StringBuilder rows = new StringBuilder();
                        rows.append("in (");
                        for (int i = 0; i < selectTable.getRowCount(); i++) {
                            rows.append("'").append(selectTable.getValueAt(i, columnIndex)).append("', ");
                        }
                        StringSelection stringSelection = new StringSelection(rows.substring(0, rows.length() - 2) + ");");
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(stringSelection, null);

                    });
                    selectHeadersMenu.add(itemCopydataAsExptression);

                    pg.select(textArea.getText().replaceAll("\n", " "));
                }
            }
        });

        // Лимит строк в селекте из файла config.txt
        JLabel configParamsLabel = new JLabel("user " + pg.user.toUpperCase()
                + ", rows limit " + pg.rowsLimitFromConfig + ", transparency "
                + pg.guiOpacity * 100 + "%");
        configParamsLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        configParamsLabel.setBounds(515, 12, 400, 20);
        getContentPane().add(configParamsLabel);

        // Сворачивание приложения в трей
        if (SystemTray.isSupported()) {
            try {
                BufferedImage iconTray = ImageIO.read(Objects.requireNonNull(Gui.class.getResourceAsStream("/icons/tray.png")));
                final TrayIcon trayIcon = new TrayIcon(iconTray, "mrpro-pgsql-ide");
                systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);

                final PopupMenu trayMenu = new PopupMenu();
                MenuItem iShow = new MenuItem("Show");
                iShow.addActionListener(e -> {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                });
                trayMenu.add(iShow);

                MenuItem iClose = new MenuItem("Close");
                iClose.addActionListener(e -> System.exit(0));
                trayMenu.add(iClose);

                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            setVisible(true);
                            setExtendedState(JFrame.NORMAL);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            trayIcon.setPopupMenu(trayMenu);
                        }
                    }
                });
            } catch (IOException | AWTException e) {
                e.printStackTrace();
            }
        }

        tableCheckbox = new Checkbox("tables");
        tableCheckbox.setEnabled(false);
        tableCheckbox.setFocusable(false);
        tableCheckbox.setState(true);
        tableCheckbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tableCheckbox.setBounds(112, 5, 52, 22);
        getContentPane().add(tableCheckbox);
        tableCheckbox.addItemListener(e -> {
            if (viewCheckbox.getState() && !tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(3);
            } else if (viewCheckbox.getState() && tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(1);
            } else if (!viewCheckbox.getState() && !tableCheckbox.getState()) {
                executeModel.setRowCount(0);
            } else if (!viewCheckbox.getState() && tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(2);
            }
        });

        viewCheckbox = new Checkbox("views");
        viewCheckbox.setEnabled(false);
        viewCheckbox.setFocusable(false);
        viewCheckbox.setState(true);
        viewCheckbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewCheckbox.setBounds(168, 5, 50, 22);
        getContentPane().add(viewCheckbox);
        viewCheckbox.addItemListener(e -> {
            if (viewCheckbox.getState() && !tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(3);
            } else if (viewCheckbox.getState() && tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(1);
            } else if (!viewCheckbox.getState() && !tableCheckbox.getState()) {
                executeModel.setRowCount(0);
            } else if (!viewCheckbox.getState() && tableCheckbox.getState()) {
                executeModel.setRowCount(0);
                pg.getUserTables(2);
            }
        });

        //common.getFavoriteFromFile();
        this.setVisible(true);
    }

    //Headers renderers
    static class SimpleHeaderRenderer extends JLabel implements TableCellRenderer {
        public SimpleHeaderRenderer() {
            setFont(new Font("Consolas", Font.BOLD, 14));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            //setBackground(Color.RED);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            if (column == 0) {
                setForeground(new Color(0, 49, 161));
                setHorizontalAlignment(CENTER);
            } else if (column == 1) {
                setForeground(new Color(159, 95, 0));
                setHorizontalAlignment(CENTER);
            } else if (column == 3) {
                setForeground(new Color(161, 0, 0));
                setHorizontalAlignment(CENTER);
            } else if (column == 6) {
                setForeground(new Color(2, 89, 22));
                setHorizontalAlignment(CENTER);
            } else {
                setForeground(new Color(0, 0, 0));
                setHorizontalAlignment(CENTER);
            }
            return this;
        }
    }

    // Убираем иконку сортировки в заголовке
    static class KeepSortIconHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer defaultRenderer;

        public KeepSortIconHeaderRenderer(TableCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (OSValidator.getOS().equals("uni")){
                    label.setFont(new Font("Consolas", Font.BOLD, 12));
                } else {
                    label.setFont(new Font("Consolas", Font.BOLD, 14));
                }

                label.setBorder(BorderFactory.createEtchedBorder());
                //label.setBackground(new Color(234, 255, 225));
            }
            return comp;
        }
    }

    // Столбец с названием таблиц
    static class TableInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            if (column == 3 || column == 4) c.setHorizontalAlignment(LEFT);
            else c.setHorizontalAlignment(CENTER);

            if (row % 2 == 0 && column != 4) {
                c.setBackground(new Color(232, 246, 255));
            } else if (row % 2 == 1 && column != 4) {
                c.setBackground(new Color(255, 252, 232));
            } else {
                c.setBackground(new Color(255, 255, 255));
            }

            if (isSelected) {
                setBackground(new Color(175, 175, 175));
            }
            return c;
        }
    }

    // Правая панель
    static class SelectTableInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

            c.setHorizontalAlignment(CENTER);

            if (row % 2 == 0) {
                c.setBackground(new Color(232, 246, 255));
            } else {
                c.setBackground(new Color(255, 255, 255));
            }

            if (isSelected) {
                setBackground(new Color(175, 175, 175));
            }
            c.setHorizontalAlignment(LEFT);
            return c;
        }
    }
}
