
//******************************
// Adds a new drug entry row immediately before the sectionId element of a table
//******************************
function addDrug(sectionId, drug, genericOptions, drugOptions, unitOptions, deleteButtonText, suffix) {
	$hiddenOrderIndex = $j('<input type="hidden" name="newOrderKey"/>').val(suffix);
	$hiddenOrderId = $j('<input type="hidden" name="orderId:' + suffix + '"/>').val(drug.orderId);
	$genericSelector = $j('<select name="generic:' + suffix + '" onchange="limitDrug(this, \'drug'+suffix + '\');">' + genericOptions + '</select>').val(drug.generic);
	$drugSelector = $j('<select id="drug' + suffix + '" name="drug:' + suffix + '">' + drugOptions + '</select>').val(drug.drugId);
	$doseSelector = $j('<input type="text" size="6" name="dose:' + suffix + '"/>').val(drug.dose);
	$unitSelector = $j('<select name="unit:' + suffix + '">' + unitOptions + '</select>').val(drug.units);
	$frequencySelector = $j('<input type="text" name="frequency:' + suffix + '"/>').val(drug.frequency);
	$endDateSelector = $j('<input type="text" size="10" tabIndex="-1" name="autoExpireDate:' + suffix + '" onFocus=\"showCalendar(this)\"/>').val(drug.autoExpireDate);
	$instructionsSelector = $j('<input type="text" name="instructions:' + suffix + '"/>').val(drug.instructions);
	$removeButton = $j('<input type="button" value=" ' + deleteButtonText + ' " onclick="removeElement(\'addDrugRow' + drug.drugId + '-' + suffix + '\');" />');
	
	$newRow = $j('<tr id="addDrugRow' + drug.drugId + '-' + suffix + '">');
	$newRow.append($hiddenOrderIndex);
	$newRow.append($hiddenOrderId);
	$newRow.append($j('<td>').html($genericSelector));
	$newRow.append($j('<td>').html($drugSelector));
	$newRow.append($j('<td>').html($doseSelector).append($unitSelector));
	$newRow.append($j('<td>').html($frequencySelector));
	$newRow.append($j('<td>').html(drug.startDate));
	$newRow.append($j('<td>').html($endDateSelector));
	$newRow.append($j('<td>').html($instructionsSelector));
	$newRow.append($j('<td>').html($removeButton));

	$j('#'+sectionId).before($newRow);
	
	limitDrug($genericSelector, 'drug'+suffix);
}

// Removes the element with the passed id from the DOM
function removeElement(id) {
	$j('#'+id).remove();
}

// Limits the drug selector
function limitDrug(selectorElement, idOfDrugSelector) {
	$j('#'+idOfDrugSelector).hide();
	var genericId = $j(selectorElement).val();
	if (genericId != null && genericId != '') {
		if ($j('#'+idOfDrugSelector).find('.drugConcept'+genericId).size() > 0) {
			$j('#'+idOfDrugSelector).show();
			$j('#'+idOfDrugSelector + " > option").show();
			$j('#'+idOfDrugSelector + " > option").not('.drugConcept'+genericId).hide();
		}
	}
}

// Empty Function
function change() {}