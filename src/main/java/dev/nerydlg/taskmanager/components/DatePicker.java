package dev.nerydlg.taskmanager.components;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * A lightweight single-date picker: a read-only field showing the chosen date and
 * a button that drops down a month calendar. Works in {@link LocalDate}; read the
 * value with {@link #getDate()} (which may be {@code null} when nothing is chosen).
 *
 * <pre>{@code
 * DatePicker picker = new DatePicker();
 * LocalDate value = picker.getDate();               // null if unset
 * LocalDateTime dueDate = value == null ? null : value.atStartOfDay();
 * }</pre>
 */
public class DatePicker extends JPanel {

  private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Locale LOCALE = Locale.getDefault();

  private final JTextField field = new JTextField(10);
  private final JButton dropButton = new JButton("▼");
  private final JPopupMenu popup = new JPopupMenu();
  private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
  private final JPanel daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));

  private LocalDate selectedDate;
  private YearMonth shownMonth = YearMonth.now();
  private Consumer<LocalDate> onDateChange;

  public DatePicker() {
    this(null);
  }

  public DatePicker(LocalDate initial) {
    super(new BorderLayout(4, 0));

    field.setEditable(false);
    field.setToolTipText("Pick a date");
    dropButton.setMargin(new Insets(0, 6, 0, 6));
    dropButton.setFocusPainted(false);

    add(field, BorderLayout.CENTER);
    add(dropButton, BorderLayout.EAST);

    buildPopup();
    dropButton.addActionListener(e -> openPopup());

    setDate(initial);
  }

  // --- public API ------------------------------------------------------

  private static DayOfWeek[] weekOrder() {
    return new DayOfWeek[]{
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };
  }

  /**
   * The selected date, or {@code null} when nothing is chosen.
   */
  public LocalDate getDate() {
    return selectedDate;
  }

  /**
   * Sets (or clears, with {@code null}) the selected date and updates the field.
   */
  public void setDate(LocalDate date) {
    selectedDate = date;
    field.setText(date == null ? "" : date.format(DISPLAY));
    if (date != null) {
      shownMonth = YearMonth.from(date);
    }
  }

  // --- popup / calendar ------------------------------------------------

  /**
   * Optional callback invoked whenever the selected date changes.
   */
  public void setOnDateChange(Consumer<LocalDate> listener) {
    this.onDateChange = listener;
  }

  private void buildPopup() {
    JButton prev = navButton("◀");
    JButton next = navButton("▶");
    prev.addActionListener(e -> showMonth(shownMonth.minusMonths(1)));
    next.addActionListener(e -> showMonth(shownMonth.plusMonths(1)));

    JPanel header = new JPanel(new BorderLayout());
    header.add(prev, BorderLayout.WEST);
    header.add(monthLabel, BorderLayout.CENTER);
    header.add(next, BorderLayout.EAST);

    JPanel weekdays = new JPanel(new GridLayout(1, 7, 2, 2));
    for (DayOfWeek day : weekOrder()) {
      JLabel label = new JLabel(day.getDisplayName(TextStyle.SHORT, LOCALE), SwingConstants.CENTER);
      label.setFont(label.getFont().deriveFont(Font.BOLD));
      weekdays.add(label);
    }

    JPanel grid = new JPanel(new BorderLayout(0, 2));
    grid.add(weekdays, BorderLayout.NORTH);
    grid.add(daysPanel, BorderLayout.CENTER);

    JButton today = new JButton("Today");
    today.addActionListener(e -> select(LocalDate.now()));
    JButton clear = new JButton("Clear");
    clear.addActionListener(e -> select(null));
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
    footer.add(today);
    footer.add(clear);

    JPanel container = new JPanel(new BorderLayout(0, 4));
    container.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    container.add(header, BorderLayout.NORTH);
    container.add(grid, BorderLayout.CENTER);
    container.add(footer, BorderLayout.SOUTH);

    popup.add(container);
  }

  private void openPopup() {
    showMonth(selectedDate != null ? YearMonth.from(selectedDate) : YearMonth.now());
    popup.show(this, 0, getHeight());
  }

  private void showMonth(YearMonth month) {
    shownMonth = month;
    monthLabel.setText(month.getMonth().getDisplayName(TextStyle.FULL, LOCALE) + " " + month.getYear());

    daysPanel.removeAll();
    // Leading blanks so day 1 lands under the right weekday (week starts Monday).
    int leadingBlanks = shownMonth.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
    for (int i = 0; i < leadingBlanks; i++) {
      daysPanel.add(new JLabel());
    }
    LocalDate today = LocalDate.now();
    for (int day = 1; day <= shownMonth.lengthOfMonth(); day++) {
      LocalDate date = shownMonth.atDay(day);
      daysPanel.add(dayButton(date, today));
    }

    popup.pack();
    daysPanel.revalidate();
    daysPanel.repaint();
  }

  private JButton dayButton(LocalDate date, LocalDate today) {
    JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
    button.setMargin(new Insets(2, 2, 2, 2));
    button.setFocusPainted(false);
    if (date.equals(selectedDate)) {
      button.setBackground(new Color(0x1E88E5));
      button.setForeground(Color.WHITE);
      button.setOpaque(true);
    } else if (date.equals(today)) {
      button.setBorder(BorderFactory.createLineBorder(new Color(0x1E88E5)));
    }
    button.addActionListener(e -> select(date));
    return button;
  }

  private JButton navButton(String text) {
    JButton button = new JButton(text);
    button.setMargin(new Insets(2, 8, 2, 8));
    button.setFocusPainted(false);
    return button;
  }

  private void select(LocalDate date) {
    setDate(date);
    popup.setVisible(false);
    if (onDateChange != null) {
      onDateChange.accept(date);
    }
  }
}
