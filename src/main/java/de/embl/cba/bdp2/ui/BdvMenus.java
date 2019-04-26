package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.motioncorrection.BigDataTrackerGUI;
import de.embl.cba.bdp2.process.BinnedView;
import de.embl.cba.bdp2.process.ChromaticShiftCorrectionView;
import de.embl.cba.bdp2.process.CroppedView;
import de.embl.cba.bdp2.process.UnsignedByteTypeView;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BdvMenus extends JMenu implements ActionListener {

    private final SaveSelectMenu saveSelectMenu;
    private final OthersMenu othersMenu;
    private final ProcessMenu processMenu;
    private ImageViewer imageViewer;

    public BdvMenus(){
        saveSelectMenu = new SaveSelectMenu(this);
        othersMenu = new OthersMenu(this);
        processMenu = new ProcessMenu(this);
    }

    public void setImageViewer(ImageViewer viewer){
        this.imageViewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add(saveSelectMenu);
        jMenuList.add(processMenu);
        jMenuList.add(othersMenu);
        return jMenuList;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.SAVE_AS_MENU_DISPLAY_TEXT)) {
            BigDataProcessor2.executorService.submit(() -> {
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog(imageViewer);
                saveMenuDialog.setVisible(true);
            });
        }else if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.OBLIQUE_MENU_DISPLAY_TEXT)) {
            BigDataProcessor2.executorService.submit(() -> {
                ObliqueMenuDialog obliqueMenuDialog = new ObliqueMenuDialog(imageViewer);
                obliqueMenuDialog.setVisible(true);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.CROP_MENU_DISPLAY_TEXT)){
        	BigDataProcessor2.executorService.submit(() -> {
            	new CroppedView<>( imageViewer );
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.IMAGEJ_VIEW_MENU_DISPLAY_TEXT)){
            RandomAccessibleInterval permuted =
                    Views.permute(imageViewer.getRai(), DimensionOrder.Z, DimensionOrder.C);
            ImageJFunctions.show(permuted, BigDataProcessor2.executorService);
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.BIG_DATA_TRACKER_MENU_DISPLAY_TEXT)){
           BigDataProcessor2.executorService.submit(() -> {
                BigDataTrackerGUI bigDataTrackerGUI = new BigDataTrackerGUI(imageViewer);
                bigDataTrackerGUI.showDialog();
                /*
                CommandService commandService = LazyLoadingCommand.uiService.getContext().service(CommandService.class);
                commandService.run( BigDataTrackerUICommand.class, true, "imageViewer", imageViewer );
                */
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.INTERACTIVE_EIGHT_BIT_MENU_DISPLAY_TEXT)){
            BigDataProcessor2.executorService.submit(() -> {
                new UnsignedByteTypeView(imageViewer);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.INTERACTIVE_BINNING)){
            BigDataProcessor2.executorService.submit(() -> {
               new BinnedView<>(imageViewer);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
            UIDisplayConstants.CHROMATIC_SHIFT_CORRECTION_MENU_DISPLAY_TEXT)){
            BigDataProcessor2.executorService.submit(() -> {
                new ChromaticShiftCorrectionView<>(imageViewer);
        });
    }
    }
}
