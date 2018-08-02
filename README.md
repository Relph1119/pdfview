# pdfview
pdf view

启动OpenOffice服务
 cd C:\Program Files (x86)\OpenOffice 4\program
 soffice -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
 netstat -aon|findstr 8100
