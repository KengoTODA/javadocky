var $currentVersion = $('#current-version');
$('a.version').on('click', function(e){
  var selectedVersion = $(e.target).text();
  $currentVersion.text(selectedVersion);
});