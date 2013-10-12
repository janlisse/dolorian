$ ->
   $('.nav-menu-item a').each (index, element) =>     
      if $(element).attr('href') == $(location).attr('pathname')             
         $(element).parent().addClass("active")     
       
