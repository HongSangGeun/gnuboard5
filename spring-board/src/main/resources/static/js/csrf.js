(function () {
    "use strict";

    function getToken() {
        if (typeof window.APP_CSRF_TOKEN === "string") {
            return window.APP_CSRF_TOKEN;
        }
        return "";
    }

    function ensureHiddenToken(form, token) {
        if (!form || !token) {
            return;
        }
        if (form.querySelector("input[name='_csrf']")) {
            return;
        }
        var input = document.createElement("input");
        input.type = "hidden";
        input.name = "_csrf";
        input.value = token;
        form.appendChild(input);
    }

    function isPostForm(form) {
        var method = form.getAttribute("method");
        if (!method) {
            return false;
        }
        return method.toLowerCase() === "post";
    }

    function bindCsrf() {
        var token = getToken();
        if (!token) {
            return;
        }

        var forms = document.querySelectorAll("form");
        for (var i = 0; i < forms.length; i++) {
            if (isPostForm(forms[i])) {
                ensureHiddenToken(forms[i], token);
            }
        }

        if (window.jQuery && window.jQuery.ajaxSetup) {
            window.jQuery.ajaxSetup({
                headers: {
                    "X-CSRF-TOKEN": token
                }
            });
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", bindCsrf);
        return;
    }
    bindCsrf();
})();
