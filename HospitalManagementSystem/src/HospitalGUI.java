import java.awt.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.awt.print.PrinterException;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HospitalGUI {
    private final HospitalManagementSystem system;
    private final PatientManager patients;
    private final DoctorManager doctors;
    private final AppointmentManager appointments;
    private final BillingManager bills;

    private HospitalGUI(String dataDir) {
        system = new HospitalManagementSystem(
                new InputHelper(new ByteArrayInputStream(new byte[0])), dataDir);
        patients = system.getPatientManager();
        doctors = system.getDoctorManager();
        appointments = system.getAppointmentManager();
        bills = system.getBillingManager();
    }

    public static void start(String dataDir) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        JFrame frame = new JFrame("Hospital Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new LogoIcon(64).createImage());
        frame.setSize(1280, 800);
        frame.setMinimumSize(new Dimension(1050, 650));
        frame.setLocationRelativeTo(null);

        HospitalGUI gui = new HospitalGUI(dataDir);
        frame.setContentPane(gui.mainFrame());
        frame.setVisible(true);
    }

    private JPanel mainFrame() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(244, 247, 251));
        root.add(sidebar(), BorderLayout.WEST);

        CardLayout cards = new CardLayout();
        JPanel content = new JPanel(cards);
        content.setBackground(new Color(244, 247, 251));

        addPage(content, "Dashboard", dashboard());
        addPage(content, "Patient Management", patientPanel());
        addPage(content, "Doctor Management", doctorPanel());
        addPage(content, "Appointments", appointmentPanel());
        addPage(content, "Billing", billingPanel());
        addPage(content, "Hospital Statistics", statisticsPanel());

        root.add(content, BorderLayout.CENTER);
        sidebarContent = content;
        sidebarCards = cards;
        return root;
    }

    private CardLayout sidebarCards;
    private JPanel sidebarContent;
    private final Map<String, JPanel> pageComponents = new HashMap<>();

    private void addPage(JPanel content, String name, JPanel page) {
        pageComponents.put(name, page);
        content.add(page, name);
    }

    private void refreshPage(String name) {
        JPanel replacement;
        if ("Dashboard".equals(name)) replacement = dashboard();
        else if ("Patient Management".equals(name)) replacement = patientPanel();
        else if ("Doctor Management".equals(name)) replacement = doctorPanel();
        else if ("Appointments".equals(name)) replacement = appointmentPanel();
        else if ("Billing".equals(name)) replacement = billingPanel();
        else replacement = statisticsPanel();
        sidebarContent.remove(pageComponents.get(name));
        pageComponents.put(name, replacement);
        sidebarContent.add(replacement, name);
        sidebarContent.revalidate();
        sidebarContent.repaint();
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 800));
        sidebar.setBackground(new Color(32, 69, 125));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 12, 18, 12));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        JLabel hospitalIcon = new JLabel(new LogoIcon(48));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("CAREPOINT");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel title2 = new JLabel("HOSPITAL MANAGEMENT");
        title2.setForeground(Color.WHITE);
        title2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandText.add(title); brandText.add(title2);
        brand.add(hospitalIcon); brand.add(brandText);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(28));

        String[][] items = {
            {"D", "Dashboard"}, {"P", "Patient Management"}, {"Dr", "Doctor Management"},
            {"A", "Appointments"}, {"B", "Billing"}, {"S", "Hospital Statistics"}
        };
        for (int i = 0; i < items.length; i++) {
            JButton nav = navButton(items[i][0], items[i][1], i == 0);
            final String page = items[i][1];
            nav.addActionListener(e -> {
                refreshPage(page);
                sidebarCards.show(sidebarContent, page);
                for (Component c : sidebar.getComponents()) {
                    if (c instanceof JButton) c.setBackground(new Color(40, 91, 150));
                }
                nav.setBackground(Color.WHITE);
                nav.setForeground(new Color(32, 69, 125));
            });
            sidebar.add(nav);
            sidebar.add(Box.createVerticalStrut(7));
        }

        sidebar.add(Box.createVerticalGlue());
        JButton exit = new JButton("Exit System");
        exit.setForeground(Color.WHITE);
        exit.setBackground(new Color(222, 78, 67));
        exit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exit.setMaximumSize(new Dimension(196, 44));
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setBorderPainted(false);
        exit.setFocusPainted(false);
        exit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exit.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(sidebar, "Save data and exit?",
                    "Exit System", JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                system.saveData();
                System.exit(0);
            } else if (answer == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        });
        sidebar.add(exit);
        sidebar.add(Box.createVerticalStrut(12));
        return sidebar;
    }

    private JButton navButton(String key, String text, boolean active) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(10, 0));
        JLabel badge = new JLabel(key, SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(38, 38));
        badge.setOpaque(true);
        badge.setBackground(active ? new Color(41, 128, 185) : new Color(55, 111, 170));
        badge.setForeground(active ? Color.WHITE : new Color(225, 235, 245));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel label = new JLabel(text);
        label.setForeground(active ? new Color(32, 69, 125) : Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.add(badge, BorderLayout.WEST);
        button.add(label, BorderLayout.CENTER);
        button.setMaximumSize(new Dimension(196, 46));
        button.setPreferredSize(new Dimension(196, 46));
        button.setBackground(active ? Color.WHITE : new Color(40, 91, 150));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel dashboard() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(new Color(244, 247, 251));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Welcome to Hospital Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(45, 46, 51));
        JLabel date = new JLabel(LocalDate.now().toString() + " | Overview of today's hospital records");
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(new Color(130, 140, 150));
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title); titleBox.add(date);

        JTextField search = new JTextField();
        search.setToolTipText("Search by patient, doctor, appointment or bill");
        search.setPreferredSize(new Dimension(270, 36));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        header.add(titleBox, BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);
        search.addActionListener(e -> searchRecords(search.getText()));
        panel.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(900, 108));
        cards.add(statCard("TOTAL PATIENTS", String.valueOf(patients.getPatientCount()),
                new Color(73, 112, 180)));
        cards.add(statCard("TOTAL DOCTORS", String.valueOf(doctors.getDoctorCount()),
                new Color(32, 161, 146)));
        cards.add(statCard("APPOINTMENTS", String.valueOf(appointments.getCount()),
                new Color(137, 75, 191)));
        cards.add(statCard("BILLS GENERATED", String.valueOf(bills.getCount()),
                new Color(211, 111, 51)));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridBagLayout());
        tables.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);

        JPanel appointmentsBox = dashboardAppointmentsTable();
        gbc.gridx = 0; gbc.weightx = 0.70;
        tables.add(appointmentsBox, gbc);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(statusSummary());
        right.add(Box.createVerticalStrut(12));
        right.add(billingSummary());

        gbc.gridx = 1; gbc.weightx = 0.30; gbc.insets = new Insets(0, 0, 0, 0);
        tables.add(right, gbc);
        center.add(tables, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JLabel footer = new JLabel("CarePoint Hospital Management System", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(150, 158, 168));
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statCard(String name, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 229, 235)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(5, 80));
        card.add(stripe, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(accent);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 28));
        number.setForeground(accent);
        text.add(label);
        text.add(Box.createVerticalStrut(4));
        text.add(number);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private void searchRecords(String text) {
        String query = text.trim().toLowerCase();
        if (query.isEmpty()) return;
        List<String> matches = new ArrayList<>();
        for (Patient p : patients.getAllPatients()) {
            if (p.getId().toLowerCase().contains(query) || p.getName().toLowerCase().contains(query)
                    || p.getMedicalCondition().toLowerCase().contains(query)) {
                matches.add("Patient  " + p.getId() + "  " + p.getName()
                        + "  | Issue: " + p.getMedicalCondition());
            }
        }
        for (Doctor d : doctors.getAllDoctors()) {
            if (d.getId().toLowerCase().contains(query) || d.getName().toLowerCase().contains(query)
                    || d.getSpecialization().toLowerCase().contains(query)) {
                matches.add("Doctor   " + d.getId() + "  " + d.getName()
                        + "  | " + d.getSpecialization());
            }
        }
        for (Appointment a : appointments.getAllAppointments()) {
            if (a.getId().toLowerCase().contains(query) || a.getPatient().getName().toLowerCase().contains(query)
                    || a.getDoctor().getName().toLowerCase().contains(query)) {
                matches.add("Visit    " + a.getId() + "  " + a.getPatient().getName()
                        + "  | " + a.getDate() + " " + a.getTime());
            }
        }
        for (Billing b : bills.getAllBills()) {
            Patient p = patients.findById(b.getPatientId());
            if (b.getId().toLowerCase().contains(query)
                    || (p != null && p.getName().toLowerCase().contains(query))) {
                matches.add("Invoice  " + b.getId() + "  "
                        + (p == null ? b.getPatientId() : p.getName())
                        + "  | " + UI.money(b.getTotalAmount()));
            }
        }
        JTextArea results = new JTextArea(matches.isEmpty()
                ? "No records matched \"" + text.trim() + "\"."
                : String.join("\n\n", matches));
        results.setEditable(false);
        results.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        results.setBackground(new Color(248, 250, 252));
        results.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scroll = new JScrollPane(results);
        scroll.setPreferredSize(new Dimension(560, 360));
        JOptionPane.showMessageDialog(sidebarContent, scroll, "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel dashboardAppointmentsTable() {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 229, 235)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel heading = new JLabel("Upcoming Appointments");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.WEST);
        JButton viewAll = new JButton("View all");
        viewAll.setBorderPainted(false);
        viewAll.setContentAreaFilled(false);
        viewAll.setForeground(new Color(47, 118, 72));
        viewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAll.addActionListener(e -> sidebarCards.show(sidebarContent, "Appointments"));
        top.add(viewAll, BorderLayout.EAST);
        box.add(top, BorderLayout.NORTH);

        String[] columns = {"ID", "Patient", "Doctor", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Appointment> list = appointments.getAllAppointments();
        list.stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED)
                .sorted((a, b) -> a.getDate().equals(b.getDate())
                        ? a.getTime().compareTo(b.getTime())
                        : a.getDate().compareTo(b.getDate()))
                .limit(7)
                .forEach(a -> model.addRow(new Object[]{
                        a.getId(), a.getPatient().getName(), a.getDoctor().getName(),
                        a.getDate(), a.getTime(), a.getStatus().getLabel()
                }));
        JTable table = table(model);
        table.setRowHeight(36);
        table.getColumnModel().getColumn(5).setCellRenderer(statusRenderer());
        box.add(new JScrollPane(table), BorderLayout.CENTER);
        return box;
    }

    private javax.swing.table.TableCellRenderer statusRenderer() {
        return (table, value, selected, focus, row, column) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString(),
                    SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String text = label.getText();
            if ("Completed".equals(text)) {
                label.setBackground(new Color(218, 242, 224));
                label.setForeground(new Color(36, 135, 70));
            } else if ("Cancelled".equals(text)) {
                label.setBackground(new Color(250, 215, 215));
                label.setForeground(new Color(194, 57, 52));
            } else {
                label.setBackground(new Color(218, 229, 251));
                label.setForeground(new Color(45, 92, 170));
            }
            return label;
        };
    }

    private JPanel statusSummary() {
        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 229, 235)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel heading = new JLabel("Appointment Status");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        box.add(heading);
        box.add(Box.createVerticalStrut(10));
        int scheduled = appointments.countByStatus(Appointment.Status.SCHEDULED);
        int completed = appointments.countByStatus(Appointment.Status.COMPLETED);
        int cancelled = appointments.countByStatus(Appointment.Status.CANCELLED);
        box.add(statusRow("Scheduled", scheduled, new Color(44, 120, 78)));
        box.add(statusRow("Completed", completed, new Color(42, 139, 96)));
        box.add(statusRow("Cancelled", cancelled, new Color(192, 74, 68)));
        box.setMaximumSize(new Dimension(400, 150));
        box.setPreferredSize(new Dimension(340, 150));
        return box;
    }

    private JPanel statusRow(String label, int count, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel dot = new JLabel(label);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setForeground(new Color(60, 66, 75));
        JLabel value = new JLabel(String.valueOf(count));
        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        row.add(dot, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        dot.setIcon(new CircleIcon(color));
        return row;
    }

    private JPanel billingSummary() {
        JPanel box = new JPanel();
        box.setBackground(Color.WHITE);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 229, 235)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel heading = new JLabel("Billing Summary");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        box.add(heading);
        box.add(Box.createVerticalStrut(9));
        box.add(summaryLine("Revenue (paid bills)",
                UI.money(bills.getRevenueFromPaidBills()), new Color(34, 148, 80)));
        box.add(summaryLine("Outstanding balance",
                UI.money(bills.getOutstandingBalance()), new Color(194, 57, 52)));

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setOpaque(false);
        JButton patient = new JButton("+ Register Patient");
        JButton doctor = new JButton("+ Add Doctor");
        JButton appointment = new JButton("Book Appointment");
        JButton bill = new JButton("Generate Bill");
        styleAction(patient, new Color(36, 135, 191));
        styleAction(doctor, new Color(32, 161, 146));
        styleAction(appointment, new Color(142, 84, 196));
        styleAction(bill, new Color(224, 132, 35));
        patient.addActionListener(e -> { if (patientDialog(null)) sidebarCards.show(sidebarContent, "Dashboard"); });
        doctor.addActionListener(e -> { if (doctorDialog(null)) sidebarCards.show(sidebarContent, "Dashboard"); });
        appointment.addActionListener(e -> { if (appointmentDialog()) sidebarCards.show(sidebarContent, "Dashboard"); });
        bill.addActionListener(e -> { if (generateBillDialog()) sidebarCards.show(sidebarContent, "Dashboard"); });
        buttons.add(patient); buttons.add(doctor); buttons.add(appointment); buttons.add(bill);
        box.add(Box.createVerticalStrut(10));
        box.add(buttons);
        box.setMaximumSize(new Dimension(400, 210));
        box.setPreferredSize(new Dimension(340, 210));
        return box;
    }

    private JPanel summaryLine(String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 11));
        v.setForeground(valueColor);
        row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return row;
    }

    private void styleAction(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel statisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(new Color(244, 247, 251));
        JLabel title = new JLabel("Hospital Statistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridLayout(3, 3, 12, 12));
        grid.setOpaque(false);
        grid.add(statCard("TOTAL PATIENTS", String.valueOf(patients.getPatientCount()), new Color(73, 112, 180)));
        grid.add(statCard("TOTAL DOCTORS", String.valueOf(doctors.getDoctorCount()), new Color(32, 161, 146)));
        grid.add(statCard("APPOINTMENTS", String.valueOf(appointments.getCount()), new Color(137, 75, 191)));
        grid.add(statCard("SCHEDULED", String.valueOf(appointments.countByStatus(Appointment.Status.SCHEDULED)), new Color(44, 120, 78)));
        grid.add(statCard("COMPLETED", String.valueOf(appointments.countByStatus(Appointment.Status.COMPLETED)), new Color(42, 139, 96)));
        grid.add(statCard("CANCELLED", String.valueOf(appointments.countByStatus(Appointment.Status.CANCELLED)), new Color(192, 74, 68)));
        grid.add(statCard("BILLS", String.valueOf(bills.getCount()), new Color(224, 132, 35)));
        grid.add(statCard("PAID REVENUE", UI.money(bills.getRevenueFromPaidBills()), new Color(34, 148, 80)));
        grid.add(statCard("OUTSTANDING", UI.money(bills.getOutstandingBalance()), new Color(194, 57, 52)));
        panel.add(grid, BorderLayout.CENTER);
        JButton save = new JButton("Save All Records");
        styleAction(save, new Color(32, 69, 125));
        save.addActionListener(e -> {
            system.saveData();
            showInfo(panel, "All records saved successfully.");
        });
        panel.add(save, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel patientPanel() {
        String[] columns = {"ID", "Name", "Age", "Gender", "Contact", "Address", "Condition"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = table(model);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Patient p : patients.getAllPatients()) {
                model.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(),
                        p.getContactNumber(), p.getAddress(), p.getMedicalCondition()});
            }
        };
        refresh.run();

        JPanel buttons = new JPanel();
        JButton add = new JButton("Register Patient");
        JButton update = new JButton("Update Selected");
        JButton delete = new JButton("Delete Selected");
        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);

        add.addActionListener(e -> {
            if (patientDialog(null)) refresh.run();
        });
        update.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select a patient."); return; }
            Patient p = patients.findById(model.getValueAt(row, 0).toString());
            if (patientDialog(p)) refresh.run();
        });
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select a patient."); return; }
            Patient p = patients.findById(model.getValueAt(row, 0).toString());
            int answer = JOptionPane.showConfirmDialog(table,
                    "Delete patient " + p.getId() + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                try {
                    patients.deletePatient(p.getId(), true);
                    refresh.run();
                } catch (HospitalException ex) {
                    showError(table, ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private boolean patientDialog(Patient existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getName());
        JTextField age = new JTextField(existing == null ? "" : String.valueOf(existing.getAge()));
        JComboBox<String> gender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        if (existing != null) gender.setSelectedItem(existing.getGender());
        JTextField contact = new JTextField(existing == null ? "" : existing.getContactNumber());
        JTextField address = new JTextField(existing == null ? "" : existing.getAddress());
        JTextField condition = new JTextField(existing == null ? "" : existing.getMedicalCondition());
        Object[] fields = {"Name:", name, "Age:", age, "Gender:", gender,
                "Contact:", contact, "Address:", address, "Medical condition:", condition};
        if (JOptionPane.showConfirmDialog(null, fields,
                existing == null ? "Register Patient" : "Update Patient",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return false;
        try {
            int a = Integer.parseInt(age.getText().trim());
            if (existing == null) patients.registerPatient(name.getText(), a, gender.getSelectedItem().toString(),
                    contact.getText(), address.getText(), condition.getText());
            else patients.updatePatient(existing, name.getText(), a, gender.getSelectedItem().toString(),
                    contact.getText(), address.getText(), condition.getText());
            return true;
        } catch (NumberFormatException ex) {
            showError(null, "Age must be a whole number.");
        } catch (HospitalException ex) {
            showError(null, ex.getMessage());
        }
        return false;
    }

    private JPanel doctorPanel() {
        String[] columns = {"ID", "Name", "Specialization", "Contact", "Fee", "Available"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = table(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Doctor d : doctors.getAllDoctors()) {
                model.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialization(),
                        d.getContactNumber(), UI.money(d.getConsultationFee()), d.getAvailabilityLabel()});
            }
        };
        refresh.run();

        JPanel buttons = new JPanel();
        JButton add = new JButton("Add Doctor");
        JButton update = new JButton("Update Selected");
        JButton delete = new JButton("Delete Selected");
        buttons.add(add); buttons.add(update); buttons.add(delete);

        add.addActionListener(e -> { if (doctorDialog(null)) refresh.run(); });
        update.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select a doctor."); return; }
            Doctor d = doctors.findById(model.getValueAt(row, 0).toString());
            if (doctorDialog(d)) refresh.run();
        });
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select a doctor."); return; }
            Doctor d = doctors.findById(model.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(table, "Delete doctor " + d.getId() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    doctors.deleteDoctor(d.getId(), true);
                    refresh.run();
                } catch (HospitalException ex) {
                    showError(table, ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private boolean doctorDialog(Doctor existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.getName());
        JComboBox<String> spec = new JComboBox<>(DoctorManager.SPECIALIZATIONS);
        if (existing != null) spec.setSelectedItem(existing.getSpecialization());
        JTextField contact = new JTextField(existing == null ? "" : existing.getContactNumber());
        JTextField fee = new JTextField(existing == null ? "" : existing.getConsultationFee().toPlainString());
        JCheckBox available = new JCheckBox("Available", existing == null || existing.isAvailable());
        Object[] fields = {"Name:", name, "Specialization:", spec, "Contact:", contact,
                "Consultation fee:", fee, "Status:", available};
        if (JOptionPane.showConfirmDialog(null, fields,
                existing == null ? "Add Doctor" : "Update Doctor",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return false;
        try {
            BigDecimal amount = Validator.parseMoney(fee.getText());
            if (existing == null) doctors.addDoctor(name.getText(), spec.getSelectedItem().toString(),
                    contact.getText(), amount);
            else doctors.updateDoctor(existing, name.getText(), spec.getSelectedItem().toString(),
                    contact.getText(), amount, available.isSelected());
            return true;
        } catch (HospitalException ex) {
            showError(null, ex.getMessage());
        }
        return false;
    }

    private JPanel appointmentPanel() {
        String[] columns = {"ID", "Patient", "Doctor", "Date", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = table(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Appointment a : appointments.getAllAppointments()) {
                model.addRow(new Object[]{a.getId(), a.getPatient().getId() + " " + a.getPatient().getName(),
                        a.getDoctor().getId() + " " + a.getDoctor().getName(),
                        a.getDate(), a.getTime(), a.getStatus().getLabel()});
            }
        };
        refresh.run();

        JPanel buttons = new JPanel();
        JButton book = new JButton("Book Appointment");
        JButton edit = new JButton("Edit Schedule");
        JButton complete = new JButton("Mark Completed");
        JButton cancel = new JButton("Cancel Selected");
        JButton delete = new JButton("Delete Cancelled");
        buttons.add(book); buttons.add(edit); buttons.add(complete); buttons.add(cancel); buttons.add(delete);
        book.addActionListener(e -> { if (appointmentDialog()) refresh.run(); });
        edit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select an appointment."); return; }
            Appointment appointment = appointments.findById(model.getValueAt(row, 0).toString());
            if (scheduleDialog(appointment)) refresh.run();
        });
        complete.addActionListener(e -> statusAction(table, model, true, refresh));
        cancel.addActionListener(e -> statusAction(table, model, false, refresh));
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo(table, "Please select an appointment."); return; }
            String id = model.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(table,
                    "Permanently delete cancelled appointment " + id + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    appointments.deleteCancelledAppointment(id);
                    refresh.run();
                } catch (HospitalException ex) {
                    showError(table, ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private boolean appointmentDialog() {
        String[] patientIds = patients.getAllPatients().stream()
                .map(p -> p.getId() + " - " + p.getName()).toArray(String[]::new);
        String[] doctorIds = doctors.getAllDoctors().stream()
                .map(d -> d.getId() + " - " + d.getName()).toArray(String[]::new);
        if (patientIds.length == 0 || doctorIds.length == 0) {
            showError(null, "Register at least one patient and one doctor first.");
            return false;
        }

        JComboBox<String> patient = new JComboBox<>(patientIds);
        JComboBox<String> doctor = new JComboBox<>(doctorIds);
        JTextField date = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField time = new JTextField("10:00");
        Object[] fields = {"Patient:", patient, "Doctor:", doctor,
                "Date (YYYY-MM-DD):", date, "Time (HH:MM):", time};
        if (JOptionPane.showConfirmDialog(null, fields, "Book Appointment",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return false;
        try {
            String patientId = patient.getSelectedItem().toString().split(" - ")[0];
            String doctorId = doctor.getSelectedItem().toString().split(" - ")[0];
            appointments.bookAppointment(patientId, doctorId,
                    LocalDate.parse(date.getText().trim()), LocalTime.parse(time.getText().trim()));
            return true;
        } catch (HospitalException | DateTimeParseException ex) {
            showError(null, ex.getMessage());
        }
        return false;
    }

    private boolean scheduleDialog(Appointment appointment) {
        if (appointment == null) return false;
        JTextField date = new JTextField(appointment.getDate().toString());
        JTextField time = new JTextField(appointment.getTime().toString());
        Object[] fields = {"Patient:", appointment.getPatient().getName(),
                "Doctor:", appointment.getDoctor().getName(),
                "Date (YYYY-MM-DD):", date, "Time (HH:MM):", time};
        if (JOptionPane.showConfirmDialog(null, fields, "Edit Appointment Schedule",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return false;
        try {
            appointments.updateSchedule(appointment.getId(), LocalDate.parse(date.getText().trim()),
                    LocalTime.parse(time.getText().trim()));
            return true;
        } catch (HospitalException | DateTimeParseException ex) {
            showError(null, ex.getMessage());
            return false;
        }
    }

    private void statusAction(JTable table, DefaultTableModel model, boolean complete, Runnable refresh) {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo(table, "Please select an appointment."); return; }
        String id = model.getValueAt(row, 0).toString();
        try {
            if (complete) appointments.completeAppointment(id);
            else appointments.cancelAppointment(id);
            refresh.run();
        } catch (HospitalException ex) {
            showError(table, ex.getMessage());
        }
    }

    private JPanel billingPanel() {
        String[] columns = {"Bill ID", "Patient", "Issue / Reason", "Appointment", "Doctor",
                "Consultation Fee", "Extra Charges", "Total", "Paid", "Balance", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = table(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Billing b : bills.getAllBills()) {
                Appointment appointment = appointments.findById(b.getAppointmentId());
                Patient patient = patients.findById(b.getPatientId());
                model.addRow(new Object[]{b.getId(), patient == null ? b.getPatientId() : patient.getName(),
                        patient == null ? "" : patient.getMedicalCondition(), b.getAppointmentId(),
                        appointment == null ? "" : appointment.getDoctor().getName(),
                        UI.money(b.getConsultationFee()), UI.money(b.getAdditionalCharges()),
                        UI.money(b.getTotalAmount()), UI.money(b.getAmountPaid()),
                        UI.money(b.getBalance()), b.getPaymentStatus().getLabel()});
            }
        };
        refresh.run();

        JPanel buttons = new JPanel();
        JButton generate = new JButton("Generate Bill");
        JButton paid = new JButton("Mark Paid");
        JButton partial = new JButton("Record Partial Payment");
        JButton unpaid = new JButton("Mark Unpaid");
        JButton edit = new JButton("Edit Extra Charges");
        JButton delete = new JButton("Delete Unpaid Bill");
        JButton print = new JButton("Print Selected Bill");
        buttons.add(generate); buttons.add(edit); buttons.add(delete);
        buttons.add(paid); buttons.add(partial); buttons.add(unpaid); buttons.add(print);

        generate.addActionListener(e -> {
            if (generateBillDialog()) refresh.run();
        });
        paid.addActionListener(e -> billingAction(table, model, "paid", refresh));
        partial.addActionListener(e -> billingAction(table, model, "partial", refresh));
        unpaid.addActionListener(e -> billingAction(table, model, "unpaid", refresh));
        edit.addActionListener(e -> editBillAction(table, model, refresh));
        delete.addActionListener(e -> deleteBillAction(table, model, refresh));
        print.addActionListener(e -> printBillAction(table, model));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void editBillAction(JTable table, DefaultTableModel model, Runnable refresh) {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo(table, "Please select a bill."); return; }
        Billing bill = bills.findById(model.getValueAt(row, 0).toString());
        String value = JOptionPane.showInputDialog(table, "New additional charges:",
                bill.getAdditionalCharges().toPlainString());
        if (value == null) return;
        try {
            bills.updateAdditionalCharges(bill.getId(), Validator.parseMoney(value));
            refresh.run();
        } catch (HospitalException ex) {
            showError(table, ex.getMessage());
        }
    }

    private void deleteBillAction(JTable table, DefaultTableModel model, Runnable refresh) {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo(table, "Please select a bill."); return; }
        String id = model.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(table, "Delete unpaid bill " + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            bills.deleteUnpaidBill(id);
            refresh.run();
        } catch (HospitalException ex) {
            showError(table, ex.getMessage());
        }
    }

    private void printBillAction(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo(table, "Please select a bill to print."); return; }
        Billing bill = bills.findById(model.getValueAt(row, 0).toString());
        showBillPreview(table, bill);
    }

    private void showBillPreview(Component parent, Billing bill) {
        Appointment appointment = appointments.findById(bill.getAppointmentId());
        Patient patient = patients.findById(bill.getPatientId());
        if (appointment == null || patient == null) {
            showError(parent, "The patient or appointment record for this bill is missing.");
            return;
        }
        String html = "<html><body style='font-family:Segoe UI,Arial; color:#263445; padding:24px'>"
                + "<div style='text-align:center; color:#176f78'><h1>CAREPOINT</h1>"
                + "<h2>HOSPITAL - OFFICIAL INVOICE</h2></div><hr>"
                + "<table width='100%'><tr><td><b>Bill No:</b> " + html(bill.getId())
                + "</td><td align='right'><b>Issued:</b> " + LocalDate.now() + "</td></tr>"
                + "<tr><td><b>Appointment:</b> " + html(appointment.getId())
                + "</td><td align='right'><b>Visit:</b> " + appointment.getDate() + "</td></tr></table>"
                + "<h3>Patient Details</h3><b>Name:</b> " + html(patient.getName())
                + "<br><b>Patient ID:</b> " + html(patient.getId())
                + "<br><b>Medical issue / reason:</b> " + html(patient.getMedicalCondition())
                + "<br><b>Doctor:</b> " + html(appointment.getDoctor().getName())
                + " (" + html(appointment.getDoctor().getSpecialization()) + ")"
                + "<h3>Charges</h3><table width='100%' cellspacing='0' cellpadding='8' border='1'>"
                + "<tr style='background:#e8f1f3'><th align='left'>Description</th><th align='right'>Amount</th></tr>"
                + "<tr><td>Consultation fee</td><td align='right'>" + html(UI.money(bill.getConsultationFee())) + "</td></tr>"
                + "<tr><td>Additional charges</td><td align='right'>" + html(UI.money(bill.getAdditionalCharges())) + "</td></tr>"
                + "<tr><th align='left'>Total amount</th><th align='right'>" + html(UI.money(bill.getTotalAmount())) + "</th></tr>"
                + "<tr><td>Amount paid</td><td align='right'>" + html(UI.money(bill.getAmountPaid())) + "</td></tr>"
                + "<tr><th align='left'>Balance due</th><th align='right'>" + html(UI.money(bill.getBalance())) + "</th></tr>"
                + "</table><p><b>Payment status:</b> " + html(bill.getPaymentStatus().getLabel())
                + "</p><hr><p style='text-align:center;color:#718096'>Thank you for choosing CarePoint.</p>"
                + "</body></html>";
        JTextPane preview = new JTextPane();
        preview.setContentType("text/html");
        preview.setText(html);
        preview.setEditable(false);
        preview.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(preview);
        scroll.setPreferredSize(new Dimension(560, 620));
        int choice = JOptionPane.showOptionDialog(parent, scroll, "Invoice " + bill.getId(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new Object[]{"Print Invoice", "Close"}, "Print Invoice");
        if (choice == 0) {
            try {
                preview.print(null, null, true, null, null, true);
            } catch (PrinterException ex) {
                showError(parent, "Could not print invoice: " + ex.getMessage());
            }
        }
    }

    private String html(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private boolean generateBillDialog() {
        List<Appointment> completed = appointments.getAllAppointments().stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED
                        && bills.findByAppointment(a.getId()) == null).toList();
        if (completed.isEmpty()) {
            showError(null, "No completed appointments are available.");
            return false;
        }
        String[] ids = completed.stream()
                .map(a -> a.getId() + " - " + a.getPatient().getName()).toArray(String[]::new);
        JComboBox<String> appointment = new JComboBox<>(ids);
        JTextField extra = new JTextField("0");
        Object[] fields = {"Completed appointment:", appointment, "Additional charges:", extra};
        if (JOptionPane.showConfirmDialog(null, fields, "Generate Bill",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return false;
        try {
            String appointmentId = appointment.getSelectedItem().toString().split(" - ")[0];
            Billing generated = bills.generateBill(appointmentId, Validator.parseMoney(extra.getText()));
            showBillPreview(null, generated);
            return true;
        } catch (HospitalException ex) {
            showError(null, ex.getMessage());
        }
        return false;
    }

    private void billingAction(JTable table, DefaultTableModel model, String action, Runnable refresh) {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo(table, "Please select a bill."); return; }
        String id = model.getValueAt(row, 0).toString();
        try {
            if (action.equals("paid")) bills.markPaid(id);
            else if (action.equals("unpaid")) bills.markUnpaid(id);
            else {
                String input = JOptionPane.showInputDialog(table, "Total amount paid:");
                if (input == null) return;
                bills.markPartiallyPaid(id, Validator.parseMoney(input));
            }
            refresh.run();
        } catch (HospitalException ex) {
            showError(table, ex.getMessage());
        }
    }

    private JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return table;
    }

    private static class CircleIcon implements javax.swing.Icon {
        private final Color color;

        CircleIcon(Color color) {
            this.color = color;
        }

        @Override public int getIconWidth() { return 12; }
        @Override public int getIconHeight() { return 12; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y + 2, 8, 8);
        }
    }

    private static class LogoIcon implements javax.swing.Icon {
        private final int size;

        LogoIcon(int size) {
            this.size = size;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = Math.max(1, size / 24);
            g.setColor(new Color(17, 137, 147));
            g.fillRoundRect(x + pad, y + pad, size - pad * 2, size - pad * 2,
                    size / 4, size / 4);
            int center = size / 2;
            int arm = Math.max(3, size / 7);
            int length = size * 3 / 5;
            g.setColor(Color.WHITE);
            g.fillRoundRect(x + center - arm / 2, y + center - length / 2,
                    arm, length, arm, arm);
            g.fillRoundRect(x + center - length / 2, y + center - arm / 2,
                    length, arm, arm, arm);
            g.dispose();
        }

        BufferedImage createImage() {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Hospital Management",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
