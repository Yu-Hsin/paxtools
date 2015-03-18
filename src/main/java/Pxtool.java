import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import org.biopax.paxtools.impl.level3.BindingFeatureImpl;
import org.biopax.paxtools.impl.level3.CellularLocationVocabularyImpl;
import org.biopax.paxtools.impl.level3.FragmentFeatureImpl;
import org.biopax.paxtools.impl.level3.ModificationFeatureImpl;
import org.biopax.paxtools.impl.level3.SequenceIntervalImpl;
import org.biopax.paxtools.impl.level3.SequenceLocationImpl;
import org.biopax.paxtools.impl.level3.SequenceSiteImpl;
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
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;

public class Pxtool {

    public static void main(String[] args) {
	// Load a sample test BioPAX File via Simple IO Handler
	FileInputStream fin;
	try {
	    fin = new FileInputStream("data/testmodel.owl");
	    BioPAXIOHandler handler = new SimpleIOHandler();
	    Model model = handler.convertFromOWL(fin); // read a model
	    
	    // get all the conversion from the model 
	    for (Conversion conversion : model.getObjects(Conversion.class)) {
		Set<PhysicalEntity> left = conversion.getLeft(); // get left part 
		Set<PhysicalEntity> right = conversion.getRight(); // get right part
		Set<Control> ctrl = conversion.getControlledOf(); // get controllers

		String l = traverse(left);
		String r = traverse(right);
		
		String control = traverseCtrl(ctrl);
		if (control.length() != 0) 
		    System.out.println(l + " -> " + r + " Controller: " + control);
		else
		    System.out.println(l + " -> " + r);
		
	    }
	    /*
	    System.out.println(model.getObjects(TemplateReactionRegulation.class));
	    System.out.println(model.getObjects(TemplateReaction.class));
	     */
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

    }

    public static String traverse(Set<PhysicalEntity> pe) {
	String ans = "";
	for (PhysicalEntity tmp : pe) {
	    ans += traversePE(tmp) + " + ";
	}
	return ans.length() == 0 ? "" : ans.substring(0, ans.length() - 3); // has to remove the trailing " + "
    }

    public static String traversePE(PhysicalEntity pe) {

	// "()" for complex
	// "[]" for PE generic
	String str1 = "(", str2 = "[", str3 = ""; 
	
	// check if it is a complex - if so using getParticipants 
	if (pe instanceof Complex) { 
	    Set<PhysicalEntity> peSet = ((Complex) pe).getComponent();
	    for (PhysicalEntity tmpPE : peSet) {
		str1 += traversePE(tmpPE) + ", ";
	    }

	}
	str1 = str1.length() == 1 ? "" : str1.substring(0, str1.length() - 2);
	if (str1.length() != 0) // complete the parenthesis
	    str1 += ")";

	// check if it is a PE generic  
	if (pe.getMemberPhysicalEntity().size() != 0) {
	    Set<PhysicalEntity> peSet = pe.getMemberPhysicalEntity();
	    for (PhysicalEntity tmpPE : peSet)
		str2 += traversePE(tmpPE) + ", ";
	}
	str2 = str2.length() == 1 ? "" : str2.substring(0, str2.length() - 2);

	if (str2.length() != 0)
	    str2 += "]";

	// not a complex and PE generic
	if (!(pe instanceof Complex)
		&& pe.getMemberPhysicalEntity().size() == 0) {

	    String name = "";
	    Set<String> nameSet = pe.getName();
	    for (String tmp : nameSet) {
		name = tmp;
		break;
	    }
	    str3 += name + ", ";
	    str3 = str3.length() == 0 ? "" : str3.substring(0, str3.length() - 2);
	    
	    Set <EntityFeature> ef = pe.getFeature();
	    if (pe.getCellularLocation() != null)
		System.out.println("subcelluar: " + pe.getCellularLocation().getTerm());
	    
	    for (EntityFeature tmpEf : ef) {
		if (tmpEf instanceof ModificationFeatureImpl) {
		    //System.out.println("modification type: " + ((ModificationFeatureImpl) tmpEf).getModificationType().getTerm());
		    for (String tmpN : ((ModificationFeatureImpl) tmpEf).getModificationType().getTerm()) {
			str3 += " (Type: " + tmpN + ")";
			break;
		    }
		    
		}
		if (tmpEf instanceof FragmentFeatureImpl)
		    System.out.println("fragment feature: " + ((FragmentFeatureImpl)tmpEf));
		
		/*if (tmpEf instanceof BindingFeatureImpl)
		    System.out.println(((BindingFeatureImpl) tmpEf).getBindsTo());*/
		
		if (tmpEf.getFeatureLocation() != null) {
		    System.out.println("AAAAAAAA");
		    if (tmpEf.getFeatureLocation() instanceof SequenceSiteImpl)
			System.out.println("Location: " + tmpEf.getFeatureLocation().toString());
		    if (tmpEf.getFeatureLocation() instanceof SequenceIntervalImpl) {
			System.out.println("Location start: " + ((SequenceIntervalImpl)tmpEf.getFeatureLocation()).getSequenceIntervalBegin().toString());
			System.out.println("Location end: " + ((SequenceIntervalImpl)tmpEf.getFeatureLocation()).getSequenceIntervalEnd().toString());
		    }
		}
	    }
	    
	}
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
