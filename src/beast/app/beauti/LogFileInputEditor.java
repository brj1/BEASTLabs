package beast.app.beauti;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;

import beast.app.util.LogFile;
import beast.app.util.Utils;
import beast.core.BEASTInterface;
import beast.core.Input;

public class LogFileInputEditor extends FileInputEditor {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Class<?> type() {
		return LogFile.class;
	}

	public LogFileInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
		super.init(input, plugin, itemNr, bExpandOption, bAddButtons);
		m_entry.setText(((File) m_input.get()).getName());
		
		JButton button = new JButton("browse");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				File defaultFile;
				if (((File) m_input.get()).exists()) {
					defaultFile = (File) m_input.get();
				} else {
					defaultFile = new File(Beauti.g_sDir);
				}
				File file = Utils.getLoadFile(m_input.getTipText(), defaultFile, "trace files", "log");
				if (file !=  null)
					file = new LogFile(file.getPath());
				try {
					m_entry.setText(file.getName());
					m_input.setValue(file, m_beastObject);
					String path = file.getPath();
					Beauti.g_sDir = path;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		add(button);
	}

}
