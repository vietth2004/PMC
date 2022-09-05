package GUI;

import cia.api.CppApi;
import cia.struct.graph.Graph;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

public class CIA
{
    @FXML
    public Button btnCompare;

    public void initialize()
    {
        // initialization code here...
        Path currentRelativePath = Paths.get("");
        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";

//        txtSourceFolder.setText(path);
    }

    @FXML
    protected void btnCompare_Clicked(ActionEvent event) throws Exception
    {
        Graph graphv1 = CppApi.buildGppProject(Paths.get("F:\\TestWorkingSpace"),Paths.get("C:\\MinGW" +
                "\\bin\\g++.exe")
        );

        Graph graphv2 = CppApi.buildGppProject(Paths.get("F:\\VietData\\thv_vnu.edu.vn\\UET\\Viet bao\\05. Regress" +
                "ionTestGen_TapChi\\TestData\\Regression\\C-Algorithms\\v1.2.0\\src"),Paths.get("C:\\MinGW\\bin\\g++" +
                ".exe")
        );

        System.out.println(graphv1.toString());
        System.out.println(graphv2.toString());


//        System.out.println("btnBrowseInput_Clicked started");
//        JFileChooser _fileChooser = new JFileChooser();
//        _fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//        Path currentRelativePath = Paths.get("");
//        String path = currentRelativePath.toAbsolutePath().toString() + "\\data-test\\Sample_for_R1_2";
//
//        _fileChooser.setSelectedFile(new File(path));
//        if (_fileChooser.showDialog(this, "Choose folder") == JFileChooser.APPROVE_OPTION)
//        {
//            String selectedPath = _fileChooser.getSelectedFile().getAbsolutePath();
//
//            //txtSourceFolder.setText(selectedPath);
//        }
    }
}
