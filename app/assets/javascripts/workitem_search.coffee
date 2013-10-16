$ ->
   $('#rangeSelect').change -> 
      range = getRange($(@).val())
      document.location.href = '/workItems/list?'+rangeToUrlParams(range)      
   
   range = getParameterByName('range')
   $('#rangeSelect').val(range) if range
   
   $('#reportButton').click (event) ->
      range = getRange($('#rangeSelect').val())
      projectId = $('.tab-pane').attr('rel')
      document.location.href = '/workItems/export?'+rangeToUrlParams(range)+"&projectId="+projectId


rangeToUrlParams = (range) ->
   'start='+range.start.format('YYYY-MM-DD')+'&end='+range.end.format('YYYY-MM-DD')+'&range='+range.name
    
getRange = (rangeOption) ->
   if rangeOption is 'lastMonth'
      return getLastMonthRange()
   if rangeOption is 'currentMonth'
      return getCurrentMonthRange()
         
getLastMonthRange = () -> 
   range = {
      name: 'lastMonth'
      start  : moment().startOf('month').subtract('months', 1)
      end : moment().startOf('month')
   }

getCurrentMonthRange = () -> 
   range = {
      name: 'currentMonth'
      start  : moment().startOf('month')
      end : moment().startOf('month').add('months', 1)
   }
	        
getParameterByName = (name) ->
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]")
    regex = new RegExp("[\\?&]" + name + "=([^&#]*)")
    results = regex.exec(location.search)
    decodeURIComponent(results[1].replace(/\+/g, " "))  if (results) 
        
	