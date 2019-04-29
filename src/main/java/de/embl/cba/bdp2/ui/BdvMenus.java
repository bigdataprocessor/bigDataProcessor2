package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.process.*;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerging;
import de.embl.cba.bdp2.tracking.BigDataTrackerGUI;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BdvMenus
        extends JMenu implements ActionListener {

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
        if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.SAVE_AS_MENU_ITEM )) {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog(imageViewer);
                saveMenuDialog.setVisible(true);
            });
        }else if (e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.OBLIQUE_MENU_ITEM )) {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                ObliqueMenuDialog obliqueMenuDialog = new ObliqueMenuDialog(imageViewer);
                obliqueMenuDialog.setVisible(true);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(UIDisplayConstants.CROP_MENU_ITEM )){
        	BigDataProcessor2.generalThreadPool.submit(() -> {
            	new Cropping<>( imageViewer );
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.IMAGEJ_VIEW_MENU_ITEM )){
            // TODO: improve this:
            // - make own class
            // - add calibration
            RandomAccessibleInterval permuted =
                    Views.permute( imageViewer.getImage().getRai(),
                            DimensionOrder.Z, DimensionOrder.C);
            ImageJFunctions.show(permuted, BigDataProcessor2.generalThreadPool);
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.CORRECT_MOTION_MENU_ITEM )){
           BigDataProcessor2.generalThreadPool.submit(() -> {
                BigDataTrackerGUI bigDataTrackerGUI = new BigDataTrackerGUI(imageViewer);
                bigDataTrackerGUI.showDialog();
                /*
                CommandService commandService = LazyLoadingCommand.uiService.getContext().service(CommandService.class);
                commandService.merge( BigDataTrackerUICommand.class, true, "imageViewer", imageViewer );
                */
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.EIGHT_BIT_CONVERSION_MENU_ITEM )){
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new UnsignedByteTypeConversion(imageViewer);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.BINNING_MENU_ITEM )){
            BigDataProcessor2.generalThreadPool.submit(() -> {
               new Binning<>(imageViewer);
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
            UIDisplayConstants.SPLIT_VIEW_MENU_ITEM )){
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new ChromaticShiftCorrection<>( imageViewer );
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                    UIDisplayConstants.SPLIT_VIEW_MENU_ITEM )){
                BigDataProcessor2.generalThreadPool.submit(() -> {
                    new SplitViewMerging( ( BdvImageViewer ) imageViewer );
        });
    }
    }
}
