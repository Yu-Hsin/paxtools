import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.TemplateReaction;

public class Pxtool {



    public static void main(String[] args) {
	// Load a sample test BioPAX File via Simple IO Handler
	FileInputStream fin;
	try {
	    fin = new FileInputStream("data/testmodel.owl");
	    BioPAXIOHandler handler = new SimpleIOHandler();
	    Model model = handler.convertFromOWL(fin); // read a model
	    for (Conversion conversion : model.getObjects(Conversion.class)) {
		Set<PhysicalEntity> left = conversion.getLeft();
		Set<PhysicalEntity> right = conversion.getRight();
		Set<Control> ctrl = conversion.getControlledOf();

		String l = traverse(left);
		String r = traverse(right);
		String control = traverseCtrl(ctrl);
		if (control.length() != 0) 
		    System.out.println(l + " -> " + r + " Controller: " + control);
		else
		    System.out.println(l + " -> " + r);

	    }
	    
	    
	    for (TemplateReaction tr : model.getObjects(TemplateReaction.class)) {
		Set <PhysicalEntity> pe = tr.getProduct();
	    }

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

    }

    public static String traverse(Set<PhysicalEntity> pe) {
	String ans = "";
	for (PhysicalEntity tmp : pe) {
	    ans += traversePE(tmp) + " + ";
	}
	return ans.length() == 0 ? "" : ans.substring(0, ans.length() - 3);
    }

    public static String traversePE(PhysicalEntity pe) {

	String str1 = "(", str2 = "[", str3 = "";

	if (pe instanceof Complex) {
	    Set<PhysicalEntity> peSet = ((Complex) pe).getComponent();
	    for (PhysicalEntity tmpPE : peSet) {
		str1 += traversePE(tmpPE) + ", ";
	    }

	}
	str1 = str1.length() == 1 ? "" : str1.substring(0, str1.length() - 2);
	if (str1.length() != 0)
	    str1 += ")";

	if (pe.getMemberPhysicalEntity().size() != 0) {
	    Set<PhysicalEntity> peSet = pe.getMemberPhysicalEntity();
	    for (PhysicalEntity tmpPE : peSet)
		str2 += traversePE(tmpPE) + ", ";
	}
	str2 = str2.length() == 1 ? "" : str2.substring(0, str2.length() - 2);

	if (str2.length() != 0)
	    str2 += "]";

	if (!(pe instanceof Complex)
		&& pe.getMemberPhysicalEntity().size() == 0) {

	    
	    String name = "";
	    Set<String> nameSet = pe.getName();
	    for (String tmp : nameSet) {
		name = tmp;
		break;
	    }
	    str3 += name + ", ";

	    Set <EntityFeature> ef = pe.getFeature();
	    System.out.println(ef);
	}
	str3 = str3.length() == 0 ? "" : str3.substring(0, str3.length() - 2);

	String ans = "";
	if (str1.length() != 0)
	    ans += str1;
	if (str2.length() != 0)
	    ans += ans.length() != 0 ? ", " + str2 : str2;
	if (str3.length() != 0)
	    ans += ans.length() != 0 ? ", " + str3 : str3;
	return ans;
    }
    
    public static String traverseCtrl(Set<Control> ctrl) {
	String ans = "";
	for (Control control : ctrl) {
	    Set<Controller> con = control.getController();
	    for (Controller tmp : con) {
		String name = "";
		Set<String> nameSet = tmp.getName();
		for (String tmpName : nameSet) {
		    name = tmpName;
		    break;
		}
		ans += name + ", ";
	    }
	}
	return ans.length() == 0 ? "" : ans.substring(0, ans.length() - 2);
    }
}
