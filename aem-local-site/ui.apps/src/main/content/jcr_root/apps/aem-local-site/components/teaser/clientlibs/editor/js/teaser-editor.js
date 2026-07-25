(function ($, document) {
    "use strict";
    $(document).on("dialog-loaded", function(e) {
        console.log("On Dialog Loaded Event Triggered");
        
        var $pretitleInput = $(e.target).find('input[name="./pretitle"]');
        if ($pretitleInput.length > 0) {
            var $wrapper = $pretitleInput.closest('.coral-Form-fieldwrapper');
            if ($wrapper.length > 0) {
                $wrapper.hide(); 
            }
        } else {
            console.log("Pretitle field not found in this dialog.");
        }
    });
})(Granite.$, document);