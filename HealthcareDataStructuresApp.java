import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

/**
 * COSC 214 Final Project - Evaluating Data Structures Using a Healthcare Dataset
 *
 * This single Java file implements four data structures from scratch:
 * 1. Dynamic Array
 * 2. Linked List
 * 3. Hash Table
 * 4. Priority Queue
 *
 * Each structure stores PatientRecord objects and supports insert, search, delete,
 * and traversal. The GUI allows the user to load sample records, add records,
 * search by ID, delete by ID, process admissions by priority, and run a benchmark.
 */
public class HealthcareDataStructuresApp {

    /** PatientRecord models one simplified row from the healthcare dataset. */
    static class PatientRecord {
        int id;
        int age;
        String gender;
        String medicalCondition;
        String hospital;
        String admissionType;
        double billingAmount;

        PatientRecord(int id, int age, String gender, String medicalCondition,
                      String hospital, String admissionType, double billingAmount) {
            this.id = id;
            this.age = age;
            this.gender = gender;
            this.medicalCondition = medicalCondition;
            this.hospital = hospital;
            this.admissionType = admissionType;
            this.billingAmount = billingAmount;
        }

        /** Emergency admissions receive higher priority in the priority queue. */
        int priority() {
            if (admissionType == null) return 1;
            String type = admissionType.toLowerCase();
            if (type.contains("emergency") || type.contains("urgent")) return 3;
            if (type.contains("elective")) return 1;
            return 2;
        }

        public String toString() {
            return "ID=" + id + ", Age=" + age + ", Gender=" + gender +
                    ", Condition=" + medicalCondition + ", Hospital=" + hospital +
                    ", Admission=" + admissionType + ", Billing=$" + String.format("%.2f", billingAmount);
        }
    }

    interface PatientStructure {
        void insertRecord(PatientRecord record);
        PatientRecord searchRecord(int id);
        boolean deleteRecord(int id);
        PatientRecord[] traverseRecords();
        int size();
        String name();
    }

    /** DynamicArray uses a resizing array similar to ArrayList, but implemented from scratch. */
    static class DynamicArrayStructure implements PatientStructure {
        private PatientRecord[] data = new PatientRecord[10];
        private int size = 0;

        private void resize() {
            PatientRecord[] larger = new PatientRecord[data.length * 2];
            for (int i = 0; i < size; i++) larger[i] = data[i];
            data = larger;
        }

        public void insertRecord(PatientRecord record) {
            if (size == data.length) resize();
            data[size++] = record;
        }

        /** Linear search: checks records one by one until the matching ID is found. */
        public PatientRecord searchRecord(int id) {
            for (int i = 0; i < size; i++) if (data[i].id == id) return data[i];
            return null;
        }

        /** Delete shifts all later records one position left to fill the removed slot. */
        public boolean deleteRecord(int id) {
            for (int i = 0; i < size; i++) {
                if (data[i].id == id) {
                    for (int j = i; j < size - 1; j++) data[j] = data[j + 1];
                    data[--size] = null;
                    return true;
                }
            }
            return false;
        }

        public PatientRecord[] traverseRecords() {
            PatientRecord[] out = new PatientRecord[size];
            for (int i = 0; i < size; i++) out[i] = data[i];
            return out;
        }

        public int size() { return size; }
        public String name() { return "Dynamic Array"; }
    }

    /** LinkedList stores records in nodes connected by next references. */
    static class LinkedListStructure implements PatientStructure {
        static class Node {
            PatientRecord record;
            Node next;
            Node(PatientRecord record) { this.record = record; }
        }
        private Node head;
        private int size = 0;

        public void insertRecord(PatientRecord record) {
            Node node = new Node(record);
            node.next = head;
            head = node;
            size++;
        }

        /** Linear search through nodes from head to tail. */
        public PatientRecord searchRecord(int id) {
            Node current = head;
            while (current != null) {
                if (current.record.id == id) return current.record;
                current = current.next;
            }
            return null;
        }

        /** Delete relinks the previous node to skip over the deleted node. */
        public boolean deleteRecord(int id) {
            if (head == null) return false;
            if (head.record.id == id) {
                head = head.next;
                size--;
                return true;
            }
            Node current = head;
            while (current.next != null) {
                if (current.next.record.id == id) {
                    current.next = current.next.next;
                    size--;
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        public PatientRecord[] traverseRecords() {
            PatientRecord[] out = new PatientRecord[size];
            Node current = head;
            int i = 0;
            while (current != null) {
                out[i++] = current.record;
                current = current.next;
            }
            return out;
        }

        public int size() { return size; }
        public String name() { return "Linked List"; }
    }

    /** HashTableStructure maps patient ID to PatientRecord using separate chaining. */
    static class HashTableStructure implements PatientStructure {
        static class Entry {
            int key;
            PatientRecord value;
            Entry next;
            Entry(int key, PatientRecord value) { this.key = key; this.value = value; }
        }
        private Entry[] table = new Entry[101];
        private int size = 0;

        private int hash(int id) {
            return Math.abs(id) % table.length;
        }

        public void insertRecord(PatientRecord record) {
            int index = hash(record.id);
            Entry current = table[index];
            while (current != null) {
                if (current.key == record.id) {
                    current.value = record;
                    return;
                }
                current = current.next;
            }
            Entry entry = new Entry(record.id, record);
            entry.next = table[index];
            table[index] = entry;
            size++;
        }

        /** Average-case O(1) search when records are spread well across buckets. */
        public PatientRecord searchRecord(int id) {
            Entry current = table[hash(id)];
            while (current != null) {
                if (current.key == id) return current.value;
                current = current.next;
            }
            return null;
        }

        public boolean deleteRecord(int id) {
            int index = hash(id);
            Entry current = table[index];
            Entry previous = null;
            while (current != null) {
                if (current.key == id) {
                    if (previous == null) table[index] = current.next;
                    else previous.next = current.next;
                    size--;
                    return true;
                }
                previous = current;
                current = current.next;
            }
            return false;
        }

        public PatientRecord[] traverseRecords() {
            PatientRecord[] out = new PatientRecord[size];
            int i = 0;
            for (Entry bucket : table) {
                Entry current = bucket;
                while (current != null) {
                    out[i++] = current.value;
                    current = current.next;
                }
            }
            return out;
        }

        public int size() { return size; }
        public String name() { return "Hash Table"; }
    }

    /** PriorityQueueStructure processes emergency patients before regular admissions. */
    static class PriorityQueueStructure implements PatientStructure {
        private PatientRecord[] heap = new PatientRecord[10];
        private int size = 0;

        private void resize() {
            PatientRecord[] larger = new PatientRecord[heap.length * 2];
            for (int i = 0; i < size; i++) larger[i] = heap[i];
            heap = larger;
        }

        private boolean higherPriority(PatientRecord a, PatientRecord b) {
            if (a.priority() != b.priority()) return a.priority() > b.priority();
            return a.id < b.id;
        }

        public void insertRecord(PatientRecord record) {
            if (size == heap.length) resize();
            heap[size] = record;
            swim(size);
            size++;
        }

        private void swim(int index) {
            while (index > 0) {
                int parent = (index - 1) / 2;
                if (!higherPriority(heap[index], heap[parent])) break;
                swap(index, parent);
                index = parent;
            }
        }

        private void sink(int index) {
            while (true) {
                int left = 2 * index + 1;
                int right = 2 * index + 2;
                int best = index;
                if (left < size && higherPriority(heap[left], heap[best])) best = left;
                if (right < size && higherPriority(heap[right], heap[best])) best = right;
                if (best == index) break;
                swap(index, best);
                index = best;
            }
        }

        private void swap(int i, int j) {
            PatientRecord temp = heap[i];
            heap[i] = heap[j];
            heap[j] = temp;
        }

        /** Search is linear because heaps are optimized for priority removal, not ID lookup. */
        public PatientRecord searchRecord(int id) {
            for (int i = 0; i < size; i++) if (heap[i].id == id) return heap[i];
            return null;
        }

        public boolean deleteRecord(int id) {
            for (int i = 0; i < size; i++) {
                if (heap[i].id == id) {
                    heap[i] = heap[--size];
                    heap[size] = null;
                    if (i < size) {
                        swim(i);
                        sink(i);
                    }
                    return true;
                }
            }
            return false;
        }

        /** Removes and returns the next admission based on emergency priority. */
        public PatientRecord processNextAdmission() {
            if (size == 0) return null;
            PatientRecord top = heap[0];
            heap[0] = heap[--size];
            heap[size] = null;
            sink(0);
            return top;
        }

        public PatientRecord[] traverseRecords() {
            PatientRecord[] out = new PatientRecord[size];
            for (int i = 0; i < size; i++) out[i] = heap[i];
            return out;
        }

        public int size() { return size; }
        public String name() { return "Priority Queue"; }
    }

    /** DataManager keeps all structures synchronized so results can be compared. */
    static class DataManager {
        DynamicArrayStructure array = new DynamicArrayStructure();
        LinkedListStructure list = new LinkedListStructure();
        HashTableStructure hash = new HashTableStructure();
        PriorityQueueStructure queue = new PriorityQueueStructure();
        int nextId = 1001;

        void insert(PatientRecord r) {
            array.insertRecord(r);
            list.insertRecord(r);
            hash.insertRecord(r);
            queue.insertRecord(r);
            if (r.id >= nextId) nextId = r.id + 1;
        }

        PatientRecord search(int id) { return hash.searchRecord(id); }

        boolean delete(int id) {
            boolean a = array.deleteRecord(id);
            list.deleteRecord(id);
            hash.deleteRecord(id);
            queue.deleteRecord(id);
            return a;
        }

        PatientRecord processNextAdmission() {
            PatientRecord record = queue.processNextAdmission();
            if (record != null) {
                array.deleteRecord(record.id);
                list.deleteRecord(record.id);
                hash.deleteRecord(record.id);
            }
            return record;
        }

        PatientRecord[] records() { return array.traverseRecords(); }

        void loadSampleData() {
            insert(new PatientRecord(nextId++, 45, "Female", "Diabetes", "Howard University Hospital", "Emergency", 12500.75));
            insert(new PatientRecord(nextId++, 31, "Male", "Asthma", "MedStar Washington", "Routine", 4200.00));
            insert(new PatientRecord(nextId++, 67, "Female", "Hypertension", "George Washington Hospital", "Urgent", 8300.50));
            insert(new PatientRecord(nextId++, 52, "Male", "Heart Disease", "United Medical Center", "Emergency", 21400.25));
            insert(new PatientRecord(nextId++, 28, "Female", "Flu", "Sibley Memorial", "Elective", 980.10));
        }

        /** Optional CSV loader for the Kaggle-style dataset. Uses selected columns by header name. */
        int loadCSV(String path) throws Exception {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String headerLine = reader.readLine();
            if (headerLine == null) return 0;
            String[] headers = splitCSV(headerLine);
            int ageIdx = indexOf(headers, "Age");
            int genderIdx = indexOf(headers, "Gender");
            int conditionIdx = indexOf(headers, "Medical Condition");
            int hospitalIdx = indexOf(headers, "Hospital");
            int admissionIdx = indexOf(headers, "Admission Type");
            int billingIdx = indexOf(headers, "Billing Amount");
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCSV(line);
                try {
                    int age = Integer.parseInt(value(parts, ageIdx, "0").trim());
                    String gender = value(parts, genderIdx, "Unknown");
                    String condition = value(parts, conditionIdx, "Unknown");
                    String hospital = value(parts, hospitalIdx, "Unknown");
                    String admission = value(parts, admissionIdx, "Routine");
                    double billing = Double.parseDouble(value(parts, billingIdx, "0").replace("$", "").trim());
                    insert(new PatientRecord(nextId++, age, gender, condition, hospital, admission, billing));
                    count++;
                } catch (Exception ignored) { }
            }
            reader.close();
            return count;
        }

        private String value(String[] values, int index, String fallback) {
            if (index < 0 || index >= values.length || values[index].isEmpty()) return fallback;
            return values[index];
        }

        private int indexOf(String[] headers, String target) {
            for (int i = 0; i < headers.length; i++) if (headers[i].trim().equalsIgnoreCase(target)) return i;
            return -1;
        }

        private String[] splitCSV(String line) {
            java.util.ArrayList<String> result = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inQuotes = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"') inQuotes = !inQuotes;
                else if (c == ',' && !inQuotes) {
                    result.add(current.toString());
                    current.setLength(0);
                } else current.append(c);
            }
            result.add(current.toString());
            return result.toArray(new String[0]);
        }
    }

    /** Benchmark compares insert, search, delete, and traverse timings across structures. */
    static class Benchmark {
        static String runBenchmark() {
            int n = 5000;
            PatientRecord[] records = generateRecords(n);
            PatientStructure[] structures = {
                    new DynamicArrayStructure(),
                    new LinkedListStructure(),
                    new HashTableStructure(),
                    new PriorityQueueStructure()
            };
            StringBuilder out = new StringBuilder();
            out.append("Benchmark using ").append(n).append(" generated PatientRecord objects\n\n");
            out.append(String.format("%-18s %-12s %-12s %-12s %-12s\n", "Structure", "Insert", "Search", "Delete", "Traverse"));
            out.append("-------------------------------------------------------------------\n");
            for (PatientStructure structure : structures) {
                long start = System.nanoTime();
                for (PatientRecord r : records) structure.insertRecord(r);
                long insertTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < 1000; i++) structure.searchRecord(records[(i * 7) % n].id);
                long searchTime = System.nanoTime() - start;

                start = System.nanoTime();
                structure.traverseRecords();
                long traverseTime = System.nanoTime() - start;

                start = System.nanoTime();
                for (int i = 0; i < 500; i++) structure.deleteRecord(records[(i * 11) % n].id);
                long deleteTime = System.nanoTime() - start;

                out.append(String.format("%-18s %-12.3f %-12.3f %-12.3f %-12.3f\n",
                        structure.name(), insertTime / 1_000_000.0, searchTime / 1_000_000.0,
                        deleteTime / 1_000_000.0, traverseTime / 1_000_000.0));
            }
            out.append("\nTimes are shown in milliseconds. Hash table search is usually fastest because it uses the patient ID as the key.");
            return out.toString();
        }

        static PatientRecord[] generateRecords(int count) {
            PatientRecord[] records = new PatientRecord[count];
            Random random = new Random(214);
            String[] genders = {"Female", "Male"};
            String[] conditions = {"Diabetes", "Asthma", "Cancer", "Hypertension", "Flu", "Heart Disease"};
            String[] hospitals = {"Howard University Hospital", "MedStar Washington", "George Washington Hospital", "Sibley Memorial"};
            String[] admissionTypes = {"Emergency", "Urgent", "Routine", "Elective"};
            for (int i = 0; i < count; i++) {
                records[i] = new PatientRecord(100000 + i, 18 + random.nextInt(70),
                        genders[random.nextInt(genders.length)],
                        conditions[random.nextInt(conditions.length)],
                        hospitals[random.nextInt(hospitals.length)],
                        admissionTypes[random.nextInt(admissionTypes.length)],
                        500 + random.nextDouble() * 30000);
            }
            return records;
        }
    }

    /** Swing GUI front end for the healthcare data-structure application. */
    static class AppGUI extends JFrame {
        private final DataManager manager = new DataManager();
        private final DefaultTableModel tableModel;
        private final JTextArea outputArea = new JTextArea(9, 80);
        private final JTextField idField = new JTextField(8);
        private final JTextField ageField = new JTextField(5);
        private final JTextField genderField = new JTextField(8);
        private final JTextField conditionField = new JTextField(12);
        private final JTextField hospitalField = new JTextField(15);
        private final JComboBox<String> admissionBox = new JComboBox<>(new String[]{"Emergency", "Urgent", "Routine", "Elective"});
        private final JTextField billingField = new JTextField(8);

        AppGUI() {
            super("COSC 214 Healthcare Data Structures App");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1100, 720);
            setLocationRelativeTo(null);
            String[] cols = {"ID", "Age", "Gender", "Condition", "Hospital", "Admission", "Billing"};
            tableModel = new DefaultTableModel(cols, 0);
            JTable table = new JTable(tableModel);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildInputPanel(), BorderLayout.NORTH);
            outputArea.setEditable(false);
            outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            add(new JScrollPane(outputArea), BorderLayout.SOUTH);
            manager.loadSampleData();
            refreshTable();
            output("Loaded sample healthcare records. Use the buttons to insert, search, delete, process admissions, or benchmark.");
        }

        private JPanel buildInputPanel() {
            JPanel panel = new JPanel(new GridLayout(3, 1));
            JPanel row1 = new JPanel();
            row1.add(new JLabel("ID:")); row1.add(idField);
            row1.add(new JLabel("Age:")); row1.add(ageField);
            row1.add(new JLabel("Gender:")); row1.add(genderField);
            row1.add(new JLabel("Condition:")); row1.add(conditionField);
            row1.add(new JLabel("Hospital:")); row1.add(hospitalField);
            row1.add(new JLabel("Admission:")); row1.add(admissionBox);
            row1.add(new JLabel("Billing:")); row1.add(billingField);

            JPanel row2 = new JPanel();
            JButton addButton = new JButton("Insert Record");
            JButton searchButton = new JButton("Search by ID");
            JButton deleteButton = new JButton("Delete by ID");
            JButton processButton = new JButton("Process Next Admission");
            JButton benchmarkButton = new JButton("Run Benchmark");
            JButton loadButton = new JButton("Load CSV");
            row2.add(addButton); row2.add(searchButton); row2.add(deleteButton);
            row2.add(processButton); row2.add(benchmarkButton); row2.add(loadButton);

            JPanel row3 = new JPanel();
            row3.add(new JLabel("Tip: Emergency and urgent patients are processed first in the priority queue."));
            panel.add(row1); panel.add(row2); panel.add(row3);

            addButton.addActionListener(e -> insertFromFields());
            searchButton.addActionListener(e -> searchByID());
            deleteButton.addActionListener(e -> deleteByID());
            processButton.addActionListener(e -> processAdmission());
            benchmarkButton.addActionListener(e -> output(Benchmark.runBenchmark()));
            loadButton.addActionListener(e -> loadCSV());
            return panel;
        }

        private void insertFromFields() {
            try {
                int id = idField.getText().trim().isEmpty() ? manager.nextId++ : Integer.parseInt(idField.getText().trim());
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = genderField.getText().trim();
                String condition = conditionField.getText().trim();
                String hospital = hospitalField.getText().trim();
                String admission = (String) admissionBox.getSelectedItem();
                double billing = Double.parseDouble(billingField.getText().trim());
                PatientRecord record = new PatientRecord(id, age, gender, condition, hospital, admission, billing);
                manager.insert(record);
                refreshTable();
                output("Inserted record into Dynamic Array, Linked List, Hash Table, and Priority Queue:\n" + record);
            } catch (Exception ex) {
                output("Error: Please enter valid age and billing amount. ID may be blank for auto-generation.");
            }
        }

        private int readID() {
            return Integer.parseInt(idField.getText().trim());
        }

        private void searchByID() {
            try {
                int id = readID();
                PatientRecord record = manager.search(id);
                output(record == null ? "No record found for ID " + id : "Found by hash table search:\n" + record);
            } catch (Exception ex) { output("Enter a valid ID to search."); }
        }

        private void deleteByID() {
            try {
                int id = readID();
                boolean removed = manager.delete(id);
                refreshTable();
                output(removed ? "Deleted record " + id + " from every data structure." : "No record found for ID " + id);
            } catch (Exception ex) { output("Enter a valid ID to delete."); }
        }

        private void processAdmission() {
            PatientRecord record = manager.processNextAdmission();
            refreshTable();
            output(record == null ? "No patients waiting." : "Processed highest-priority admission:\n" + record);
        }

        private void loadCSV() {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    int count = manager.loadCSV(chooser.getSelectedFile().getAbsolutePath());
                    refreshTable();
                    output("Loaded " + count + " records from CSV and inserted them into all structures.");
                } catch (Exception ex) {
                    output("CSV load failed: " + ex.getMessage());
                }
            }
        }

        private void refreshTable() {
            tableModel.setRowCount(0);
            for (PatientRecord r : manager.records()) {
                tableModel.addRow(new Object[]{r.id, r.age, r.gender, r.medicalCondition, r.hospital, r.admissionType, String.format("$%.2f", r.billingAmount)});
            }
        }

        private void output(String text) {
            outputArea.setText(text);
        }
    }

    /** Console tests required by the implementation phase. Run with: java HealthcareDataStructuresApp test */
    static void runTests() {
        DataManager manager = new DataManager();
        manager.insert(new PatientRecord(1, 50, "Female", "Diabetes", "Hospital A", "Emergency", 1000));
        manager.insert(new PatientRecord(2, 22, "Male", "Flu", "Hospital B", "Routine", 500));
        manager.insert(new PatientRecord(3, 70, "Female", "Heart Disease", "Hospital C", "Urgent", 8000));
        System.out.println("TEST 1 Insert/Traverse: " + (manager.records().length == 3 ? "PASS" : "FAIL"));
        System.out.println("TEST 2 Search ID 2: " + (manager.search(2) != null ? "PASS" : "FAIL"));
        System.out.println("TEST 3 Delete ID 2: " + (manager.delete(2) && manager.search(2) == null ? "PASS" : "FAIL"));
        PatientRecord next = manager.processNextAdmission();
        System.out.println("TEST 4 Priority Queue Emergency First: " + (next != null && next.id == 1 ? "PASS" : "FAIL"));
        System.out.println("\n" + Benchmark.runBenchmark());
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            runTests();
        } else {
            SwingUtilities.invokeLater(() -> new AppGUI().setVisible(true));
        }
    }
}
