import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ElementTransferable implements Transferable {
    public static final DataFlavor ELEMENT_FLAVOR =
            new DataFlavor(PosterElement.class, "Poster Element");

    private PosterElement element;

    public ElementTransferable(PosterElement element) {
        this.element = element;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { ELEMENT_FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(ELEMENT_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(ELEMENT_FLAVOR)) {
            return element;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}