package org.openmrs.module.mdrtb.specimen;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.mdrtb.MdrtbFactory;
import org.openmrs.module.mdrtb.MdrtbService;


public class MdrtbDstResultImpl implements MdrtbDstResult {

	protected final Log log = LogFactory.getLog(getClass());
	
	Obs dstResult;  // the top-level obs that holds all the data for this result
	
	Concept drug; // a secondary location to store drug-type; used as a workaround for the odd way that the result and the drug are stored in a single obs
	
	Set<Concept> resultSet; // stores all the possible values for the results
	
	MdrtbFactory mdrtbFactory;
	
	public MdrtbDstResultImpl() {
		this.mdrtbFactory = Context.getService(MdrtbService.class).getMdrtbFactory();	
		this.resultSet = initResultSet();
		// also instantiate an empty obs so that we can use this as a backing object
		dstResult = new Obs();
	}
	
	// set up a dst object, given an existing obs, or an existing dst
	public MdrtbDstResultImpl(Obs dstResult) {
		this.mdrtbFactory = Context.getService(MdrtbService.class).getMdrtbFactory();
		this.resultSet = initResultSet();
				
		if(dstResult != null && dstResult.getConcept().equals(mdrtbFactory.getConceptDSTResultParent())) {
			this.dstResult = dstResult;
		}	 
		else {
			throw new RuntimeException ("Cannot initialize dst: invalid obs used for initialization.");
		}	 
	}
	
    public Integer getColonies() {
    	Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptColonies());
    	
    	if (obs == null || obs.getValueNumeric() == null) {
    		return null;
    	}
    	else {
    		return obs.getValueNumeric().intValue();
    	}
    }

    public Double getConcentration() {
    	Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptConcentration());
    	
    	if (obs == null) {
    		return null;
    	}
    	else {
    		return obs.getValueNumeric();
    	}
    }

     public Concept getDrug() {
    	 Obs obs = getResultObs();
    	 
    	if (obs == null) {
    		// return the temporary drug-type store if it has been defined
    		if(this.drug != null) {
    			return drug;
    		}
    		else {
    			return null;
    		}
    	}
    	else {
    		return obs.getValueCoded();
    	}
    }

    public Concept getResult() {
    	Obs obs = getResultObs();
   	 
    	if (obs == null) {
    		return null;
    	}
    	else {
    		return obs.getConcept();
    	}
    }

    public void setColonies(Integer colonies) {
    	Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptColonies());
    	
    	// if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && colonies == null) {
			return;
		}
    	
    	// initialize the obs if needed
		if (obs == null) {
			obs = new Obs (dstResult.getPerson(), mdrtbFactory.getConceptColonies(), dstResult.getObsDatetime(), dstResult.getLocation());
			obs.setEncounter(dstResult.getEncounter());
			dstResult.addGroupMember(obs);
		}
		
		// now set the value
		obs.setValueNumeric(colonies.doubleValue());
    }

    public void setConcentration(Double concentration) {
    	Obs obs = getObsFromObsGroup(mdrtbFactory.getConceptConcentration());
    	
    	// if this obs have not been created, and there is no data to add, do nothing
		if (obs == null && concentration == null) {
			return;
		}
    	
    	// initialize the obs if needed
		if (obs == null) {
			obs = new Obs (dstResult.getPerson(), mdrtbFactory.getConceptConcentration(), dstResult.getObsDatetime(), dstResult.getLocation());
			obs.setEncounter(dstResult.getEncounter());
			dstResult.addGroupMember(obs);
		}
		
		// now set the value
		obs.setValueNumeric(concentration);
    }

    public void setResult(Concept result) {
    	// if the result hasn't changed, there is nothing to do
    	if (getResult() == result) {
    		return;
    	}
    	
    	// TODO: is this what we really want to do in this case?
    	// if the result has been set to null, that means we need to void the entire obsgroup and remove it from it's parent
    	if(result == null) {
    		this.dstResult.setVoided(true);
    		this.dstResult.setVoidReason("voided by mdr-tb module ui");
    		for(Obs obs : dstResult.getGroupMembers()) {
    			obs.setVoided(true);
    			obs.setVoidReason("voided by mdr-tb module ui");
    		}
    		return;
    	}
    	
    	// initialize the obs if needed
    	if (getResult() == null) {
    		Obs obs = new Obs (dstResult.getPerson(), result, dstResult.getObsDatetime(), dstResult.getLocation());
    		
    		// set the drug if it has already been defined
    		if (this.drug != null) {
    			obs.setValueCoded(drug);
    			this.drug = null; // get rid of the temporary store
    		}
    		
    		dstResult.addGroupMember(obs);
    	}
    	// otherwise, just set the concept
    	else {
    		getResultObs().setConcept(result);
    	}
    }

    public void setDrug(Concept drug) {
    	// if the drug hasn't changed, there is nothing to do
    	if (getDrug() == drug) {
    		return;
    	}
    	
    	// if the result hasn't been set yet, we have to temporarily store the drug elsewhere
    	if (getResult() == null) {
    		this.drug = drug;
    	}
    	else {
    		// otherwise, set the drug
    		getResultObs().setValueCoded(drug);
    	}
    }

    /**
	 * Utility methods 
	 */
	
	/**
	 * Iterates through all the obs in the test obs group and
	 * returns the first one that who concept matches the specified concept
	 * Returns null if obs not found
	 */
    protected Obs getObsFromObsGroup(Concept concept) {
    	if (dstResult.getGroupMembers() != null) {
    		for(Obs obs : dstResult.getGroupMembers()) {
    			if (!obs.isVoided() && obs.getConcept().equals(concept)) {
    				return obs;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Initializes the resultSet property, which stores all the possible result types
     * for a DST result
     */
    private Set<Concept> initResultSet() {
    	Set<Concept> resultSet = new HashSet<Concept>();
    	
    	resultSet.add(mdrtbFactory.getConceptSusceptibleToTuberculosisDrug());
    	resultSet.add(mdrtbFactory.getConceptIntermediateToTuberculosisDrug());
    	resultSet.add(mdrtbFactory.getConceptResistantToTuberculosisDrug());
    	resultSet.add(mdrtbFactory.getConceptWaitingForTestResults());
    	resultSet.add(mdrtbFactory.getConceptDstTestContaminated());
    	
    	return resultSet;
    }

    /**
     * Returns the obs that contains the test result
     */
    // TODO: this method won't work correct if the dst result construct has more than one result concept in it (which is invalid, but we probably should test for it?)
    private Obs getResultObs() {
    	if (dstResult.getGroupMembers() != null) {    	
    		for(Obs obs : dstResult.getGroupMembers()) {
    			if (resultSet.contains(obs.getConcept())) {
    				return obs;
    			}
    		}
    	}
    	return null;
    }
}
