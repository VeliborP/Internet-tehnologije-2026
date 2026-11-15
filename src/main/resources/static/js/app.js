// Web Shop - small progressive enhancements. The application works without JavaScript as well.
(function () {
    'use strict';

    // Ask for confirmation before submitting destructive forms (data-confirm="message").
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.getAttribute('data-confirm'))) {
                event.preventDefault();
            }
        });
    });

    // Automatically hide success messages after a few seconds.
    document.querySelectorAll('.alert-success.alert-dismissible').forEach(function (alert) {
        window.setTimeout(function () {
            if (window.bootstrap && document.body.contains(alert)) {
                window.bootstrap.Alert.getOrCreateInstance(alert).close();
            }
        }, 6000);
    });

    // Prevent double submission (e.g. confirming an order twice).
    document.querySelectorAll('form[method="post"]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (event.defaultPrevented) {
                return;
            }
            if (form.dataset.submitted === 'true') {
                event.preventDefault();
                return;
            }
            form.dataset.submitted = 'true';
        });
    });
})();
