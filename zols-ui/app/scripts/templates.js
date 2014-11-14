'use strict';

(function($) {
    var base_url = 'http://localhost:8080/api';

    $.ajaxSetup({
        contentType: 'application/json'
    });

    $('[data-toggle="tooltip"]').tooltip();
    $('#templateRepositorynameLbl').hide();

    var template;
    var selectedTemplateRepository;
    var listOfCategories;
    var parentTemplates = [];
    var selectedTemplate;
    var listOfTemplates;

    var confirmationPromise;

    $('#edit_selected').on('click', function() {
        $.fn.renderTemplateRepository();
    });
    $("#del_conf_ok").on('click', function() {
        $("#delete-conf-model").modal('hide');
        confirmationPromise.resolve();
    });
    $("del_conf_cancel").on('click', function() {
        confirmationPromise.reject();
    });

    $('#delete_selected').on('click', function() {
        $("#delete-conf-model").modal('show');
        confirmationPromise = $.Deferred();
        confirmationPromise.done(function() {
            $.fn.deleteSelectedTemplateRepository();
        });
    });

    $.fn.listTemplateRepositories = function() {
        $.get(base_url + '/template_repositories').done(function(data) {
            if (data === "") {
                $('#templateRepositoryHeader').hide();
                var template = $.templates("#noTemplateRepository");
                template.link('#result', {});
                $('#result a').click(function() {
                    $.fn.createTemplateRepository();
                });
                $('#templatesBreadcrumb').empty();
                parentTemplates = [];
            } else {
                $('#templateRepositoryHeader').show();
                listOfCategories = data;
                var template = $.templates("#listTemplateRepository");
                template.link('#categories', {templateRepository: data});
                $('#createTemplateRepository').click(function() {
                    $.fn.createTemplateRepository();
                });
                $('#categories .catName').on('click', function() {
                    $.fn.setSelectedTemplateRepository($.view(this).data);
                });

                if (data.length > 0) {
                    $.fn.setSelectedTemplateRepository(data[0]);
                }
            }



        });
    };

    $.fn.setSelectedTemplateRepository = function(selectedTemplateRepositoryData) {
        parentTemplates = [];
        $('[data-bind-col="templateRepositoryname"]').text(selectedTemplateRepositoryData.label);
        selectedTemplateRepository = selectedTemplateRepositoryData;
        $('#templatesBreadcrumb').empty();
        $.get(base_url + '/template_repositories/' + selectedTemplateRepository.name + '/first_level_templates').done(function(data) {
            $.fn.listTemplates(data);
        });

    };
    $.fn.listTemplates = function(listofTemplates) {
        if (listofTemplates === "") {
            var template = $.templates("#noTemplate");
            template.link('#result', {});
            $('#result a').click(function() {
                $.fn.createTemplate();
            });
        } else {
            listOfTemplates = {link: listofTemplates};
            var template = $.templates("#listTemplate");
            template.link('#result', listOfTemplates);
            $('#addMoreTemplateBtn').on('click', function() {
                $.fn.createTemplate();
            });

            $('#result li a').on('click', function() {
                $.fn.addParentTemplate($.view(this).data);
            });

            $('#result .glyphicon-trash').on('click', function() {
                selectedTemplate = listOfTemplates.link[$(this).parent().parent().index()];
                $("#delete-conf-model").modal('show');
                confirmationPromise = $.Deferred();
                confirmationPromise.done(function() {
                    $.fn.deleteTemplate();
                });
            });

            $('#result .glyphicon-edit').on('click', function() {
                selectedTemplate = listOfTemplates.link[$(this).parent().parent().index()];
                $.fn.renderTemplate();
            });
        }




    };

    $.fn.addParentTemplate = function(ParentTemplateData) {
        parentTemplates.push(ParentTemplateData);
        $.fn.listChildren(ParentTemplateData);
    };

    $.fn.listChildren = function(ParentTemplateData) {
        $.get(base_url + '/templates/childen_of/' + ParentTemplateData.name).done(function(data) {
            $.fn.listTemplates(data);
        });
        template = $.templates("#breadcrumb");
        template.link('#templatesBreadcrumb', {parentTemplate: parentTemplates});
        $('#templatesBreadcrumb a').on('click', function() {
            var selectedTemplateIndex = $(this).parent().index();
            if (selectedTemplateIndex === 0) {
                parentTemplates = [];
                $.fn.refreshList();
            } else {
                var parentTemplateData = parentTemplates[selectedTemplateIndex - 1];
                parentTemplates = parentTemplates.slice(0, selectedTemplateIndex);
                $.fn.listChildren(parentTemplateData);
            }

        });
    };

    $.fn.saveTemplateRepository = function() {
        if (selectedTemplateRepository.isEdit) {
            delete selectedTemplateRepository.isEdit;
            $.ajax({
                method: 'PUT',
                url: base_url + '/template_repositories/' + selectedTemplateRepository.name,
                dataType: 'json',
                data: JSON.stringify(selectedTemplateRepository)
            }).done(function(data) {
                $.fn.listTemplateRepositories();
            }).error(function(data) {
                $.fn.onError(data);
            });
        }
        else {
            $.ajax({
                method: 'POST',
                url: base_url + '/template_repositories',
                dataType: 'json',
                data: JSON.stringify(selectedTemplateRepository)
            }).done(function(data) {
                $.fn.listTemplateRepositories();
            }).error(function(data) {
                $.fn.onError(data);
            });
        }

    };
    $.fn.deleteSelectedTemplateRepository = function() {
        $.ajax({
            method: 'DELETE',
            url: base_url + '/template_repositories/' + selectedTemplateRepository.name,
            dataType: 'json'
        }).done(function(data) {
            $.fn.listTemplateRepositories();
        }).error(function(data) {
            $.fn.onError(data);
        });
    };
    $.fn.renderTemplateRepository = function() {
        if (selectedTemplateRepository && selectedTemplateRepository.name) {
            selectedTemplateRepository.isEdit = true;
        }
        template = $.templates("#catetoryForm");
        template.link('#result', selectedTemplateRepository);
        $("#result form").submit(function(event) {
            event.preventDefault();
            $.fn.saveTemplateRepository();
        });
        $('#templateRepositoryHeader').hide();
    };



    $.fn.createTemplateRepository = function() {
        selectedTemplateRepository = {};
        template = $.templates("#getTemplateRepositoryType");
        template.link('#result', selectedTemplateRepository);      

        $('#result a').click(function() {
            selectedTemplateRepository.type = $(this).attr('data-type');            
            $.fn.renderTemplateRepository();
        });

    };

    $.fn.refreshList = function() {
        if (parentTemplates.length !== 0) {
            $.fn.listChildren(parentTemplates[parentTemplates.length - 1]);
        }
        else if (!listOfCategories) {
            $.fn.listTemplateRepositories();
        }
        else {
            $.fn.setSelectedTemplateRepository(selectedTemplateRepository);
        }

        $('#templateRepository-list').show();
        $('#templateRepositorynameLbl').hide();
        $('#templateRepositoryHeader').show();
    };

    $.fn.saveTemplate = function() {
        selectedTemplate.repositoryName = selectedTemplateRepository.name;
        if (parentTemplates.length !== 0) {
            selectedTemplate.parentTemplateName = parentTemplates[parentTemplates.length - 1].name;
        }

        if (selectedTemplate.isEdit) {
            delete selectedTemplate.isEdit;
            $.ajax({
                method: 'PUT',
                url: base_url + '/templates/' + selectedTemplate.name,
                dataType: 'json',
                data: JSON.stringify(selectedTemplate)
            }).done(function(data) {
                $.fn.refreshList();
            }).error(function(data) {
                $.fn.onError(data);
            });
        } else {
            $.ajax({
                method: 'POST',
                url: base_url + '/templates',
                dataType: 'json',
                data: JSON.stringify(selectedTemplate)
            }).done(function(data) {
                $.fn.refreshList();
            }).error(function(data) {
                $.fn.onError(data);
            });
        }

    };


    $.fn.deleteTemplate = function() {
        $.ajax({
            method: 'DELETE',
            url: base_url + '/templates/' + selectedTemplate.name,
            dataType: 'json'
        }).done(function(data) {
            $.fn.refreshList();
        }).error(function(data) {
            $.fn.onError(data);
        });
    };
    $.fn.renderTemplate = function() {
        if (selectedTemplate && selectedTemplate.name) {
            selectedTemplate.isEdit = true;
        }
        template = $.templates("#linkForm");
        template.link('#result', selectedTemplate);
        $("#result form").submit(function(event) {
            event.preventDefault();
            $.fn.saveTemplate();
        });
        $('#templateRepository-list').hide();
        $('#templateRepositorynameLbl').show();

    };

    $.fn.createTemplate = function() {
        selectedTemplate = {};
        $.fn.renderTemplate();
    };

    $.fn.onError = function(data) {
        $("#result").prepend('<div class="alert alert-danger"><a href="#" class="close" data-dismiss="alert">&times;</a><strong>Error ! </strong>There was a problem. Please contact admin</div>');
    };

    $.fn.listTemplateRepositories();

}(jQuery));