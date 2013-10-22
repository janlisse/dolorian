$(document).ready ->
   $('#closeButton, button.close').click (event) ->
      event.preventDefault()
      clearWorkItemForm()
      $('#saveModal').modal('hide')
         
   $('#simpleWorkItemSaveBtn').click (event) ->
      event.preventDefault()
      clearFormErrors()
      saveWorkItem()
         
   $('#saveModal').on 'shown', ->
      parsedTime = moment($('#time').text(),'HH:mm:ss')
      $('#duration').val parsedTime.format('HH:mm')
      $('#projectDisplay').val($('#projectSelect option:selected').text())
      $('#description').focus()
         
   $('#start').click ->
      $('.fadeOutOnStart').fadeOut 600
      StopWatch.start this, $('#time'),
      -> $('#stopWatchControls').slideUp(200),
      -> $('#stopWatchControls').slideDown(200)
 
   $('#reset').click ->
      resetStopwatchAndControls()
        
  
saveWorkItem = ->
   workItem =
      projectId: parseInt($('#projectSelect').val())
      duration: $('#duration').val()
      description: $('#description').val()
      date: moment().format('YYYY-MM-DD')
   route = jsRoutes.controllers.WorkItems.submitSimpleWorkItem()
   $.ajax route.url,
      type: route.method
      contentType: "application/json"
      data: JSON.stringify workItem
      success: (data) ->
         resetStopwatchAndControls()
         clearWorkItemForm()
         $('#saveModal').modal('hide')
         createSuccessMessage()
      error: (data) ->
         if data.responseText
            displayError error for error in JSON.parse data.responseText
         else
            $errorBox = $('<div/>').addClass('alert alert-error')
            message = data.statusText
            $('.modal-body').children().first().prepend $errorBox.text(message)
      
displayError = (error) ->
   fieldName = error.path.substring(1)
   $errorBox = $('<span/>').addClass('help-inline')
   input = $('#'+fieldName)
   input.after $errorBox.text(error.errors[0])
   input.parents('div.control-group').addClass('error')

clearFormErrors = ->
   $('#saveModal .help-inline').remove()
   $('#saveModal .error,#saveModal .alert-error').removeClass('error')

clearWorkItemForm = ->
   $('#description').val('')
   clearFormErrors()
   
createSuccessMessage = ->
   source = $("#success-template").html()
   template = Handlebars.compile(source)
   html = template({message: "Test"})
   $('#content').prepend(html)
   $('#content div:first').hide().fadeIn(2000).delay(5000).fadeOut()
   
resetStopwatchAndControls = ->
   StopWatch.reset()
   $('#stopWatchControls').hide()
   $('.fadeOutOnStart').show()
   
   
   
   