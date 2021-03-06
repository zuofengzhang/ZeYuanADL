
package laboratory;
/*
在许多Windows应用程序里面，最常见的是IE的地址栏，当我们在ComboBox的文本框内容时，
它的下拉列表中自动列出最匹配的项目，并且将最匹配的项目显示在输入框中。
在Java中有个JComboBox类，它可以实现下拉选择或者输入选择。
但是它本身没有提供自动查找和完成功能。我们现在就来   “改装”这个类，使它具有自动查找和完成功能。

改装思路如下：
1.先继承一个JComboBox类，将其setEditable为true.   这样的话，用户才可以在combobox上输入文字。
2.我们知道combobox的输入框是一个JTextField,   可以通过comboBox.getEditor().getEditorComponent()取得这个文本框。
3.为这个文本框加上一个KeyListener.
4.当用户在文本框中按键时，会解发keyReleased事件，我们在这个事件里写主要的实现自动查找和完成的代码。
思想就是这么简单，而自动查找的算法，任何一个对编程不陌生的人都可以写出。以下我列出完整的程序代码：
 */

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class JAutoCompleteComboBox extends JComboBox {

    private AutoCompleter completer;

    public JAutoCompleteComboBox() {
        super();
        addCompleter();
    }

    public JAutoCompleteComboBox(ComboBoxModel cm) {
        super(cm);
        addCompleter();
    }

    public JAutoCompleteComboBox(Object[] items) {
        super(items);
        addCompleter();
    }

    public JAutoCompleteComboBox(List v) {
        super((Vector) v);
        addCompleter();
    }

    private void addCompleter() {
        setEditable(true);
        completer = new AutoCompleter(this);
    }

    public void autoComplete(String str) {
        this.completer.autoComplete(str, str.length());
    }

    public String getText() {
        return ((JTextField) getEditor().getEditorComponent()).getText();
    }

    public void setText(String text) {
        ((JTextField) getEditor().getEditorComponent()).setText(text);
    }

    public boolean containsItem(String itemString) {
        for (int i = 0; i < this.getModel().getSize(); i++) {
            String _item = " " + this.getModel().getElementAt(i);
            if (_item.equals(itemString)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        JFrame frame = new JFrame();
        Object[] items = new Object[]{"abc ", "aab ", "aba ", "hpp ", "pp ", "hlp "};
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        JComboBox cmb = new JAutoCompleteComboBox(model);
        model.addElement("abc ");
        model.addElement("aab ");
        model.addElement("aba ");
        model.addElement("hpp ");
        model.addElement("pp ");
        model.addElement("hlp ");
        frame.getContentPane().add(cmb);
//        frame.setSize(400, 80);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

/**
 *   自动完成器。自动找到最匹配的项目，并排在列表的最前面。
 *   @author   Turbo   Chen
 */
class AutoCompleter implements KeyListener, ItemListener {

    private JComboBox comboBox = null;
    private JTextField textField = null;
    private ComboBoxModel comboBoxModel = null;

    public AutoCompleter(JComboBox comboBox) {
        this.comboBox = comboBox;
        textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(this);
        comboBoxModel = comboBox.getModel();
        comboBox.addItemListener(this);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        char ch = e.getKeyChar();
        if (ch == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(ch) || ch == KeyEvent.VK_DELETE) {
            return;
        }

        int caretPosition = textField.getCaretPosition();
        System.out.println(caretPosition);
        String str = textField.getText();
        if (str.length() == 0) {
            return;
        }
        autoComplete(str, caretPosition);
    }

    /**
     *   自动完成。根据输入的内容，在列表中找到相似的项目.
     */
    protected void autoComplete(String strf, int caretPosition) {
        Object[] opts;
        opts = getMatchingOptions(strf.substring(0, caretPosition));
        if (comboBox != null) {
            comboBoxModel = new DefaultComboBoxModel(opts);
            comboBox.setModel(comboBoxModel);
        }
        if (opts.length > 0) {
            textField.setCaretPosition(caretPosition);
            textField.setSelectionStart(caretPosition);
            textField.setSelectionEnd(opts.length);
            if (comboBox != null) {
                try {
                    comboBox.showPopup();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     *
     *   找到相似的项目,   并且将之排列到数组的最前面。
     *   @param   str
     *   @return   返回所有项目的列表。
     */
    protected Object[] getMatchingOptions(String str) {
        List v = new Vector();
        List v1 = new Vector();

        for (int k = 0; k < comboBoxModel.getSize(); k++) {
            Object itemObj = comboBoxModel.getElementAt(k);
            if (itemObj != null) {
                String item = itemObj.toString().toLowerCase();
                if (item.startsWith(str.toLowerCase())) {
                    v.add(comboBoxModel.getElementAt(k));
                } else {
                    v1.add(comboBoxModel.getElementAt(k));
                }
            } else {
                v1.add(comboBoxModel.getElementAt(k));
            }
        }
        for (int i = 0; i < v1.size(); i++) {
            v.add(v1.get(i));
        }
        if (v.isEmpty()) {
            v.add(str);
        }
        return v.toArray();
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            int caretPosition = textField.getCaretPosition();
            if (caretPosition != -1) {
                try {
                    textField.moveCaretPosition(caretPosition);
                } catch (IllegalArgumentException ex) {
                }
            }
        }
    }
}
