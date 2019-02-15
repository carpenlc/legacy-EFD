<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">   
   <xsl:output
       method="html"
       omit-xml-declaration="yes"
       doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
       doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
       indent="yes"/>
    
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <link rel="icon" type="image/png" href="/images/logoblue-32x32.png"/>
                
                <xsl:for-each select="combine/css">
                    <xsl:for-each select="file">
                        <link rel="stylesheet" type="text/css" href="{.}"/>
                    </xsl:for-each>
                </xsl:for-each>
                
                <script type="text/javascript" src="/dwr/engine.js"></script>
                <script type="text/javascript" src="/dwr/interface/AdminHelper.js"></script>
                <script type="text/javascript" src="/dwr/interface/AlertHelper.js"></script>
                <script type="text/javascript" src="/dwr/interface/AllowedHostService.js"></script>
                <script type="text/javascript" src="/dwr/interface/ContentSetHelper.js"></script>
                <script type="text/javascript" src="/dwr/interface/HistoryHelper.js"></script>
                <script type="text/javascript" src="/dwr/interface/Static.js"></script>
                <script type="text/javascript" src="/dwr/interface/StatusService.js"></script>
                <script type="text/javascript" src="/dwr/interface/System.js"></script>
                <script type="text/javascript" src="/dwr/interface/UserHelper.js"></script>
                
                <xsl:for-each select="combine/javascript">
                    <xsl:for-each select="file">
                        <script type="text/javascript" src="{.}"></script>
                    </xsl:for-each>
                </xsl:for-each>
                
                <script type="text/javascript">
                    dwr.engine.setNotifyServerOnPageUnload(false);
                    Ext.BLANK_IMAGE_URL = '/ext/resources/images/default/s.gif';
                    Ext.onReady(EFD.login.start);
                </script>
                
                <title>Please wait...</title>
            </head>
            <body>
                <div id="content">
                    <div id="header">
                        <img src="/images/efd-logo.png" alt="EFD Logo"/>
                        <div id="navigation">
                        	<img style="float:left;margin-right:1px;" src="/images/nav-left.png"/>
							<img style="float:right;margin-left:1px;" src="/images/nav-right.png"/>
                        </div>
                        <img id="NGA_Logo" src="/images/NGA_Logo.png" alt="NGA Logo"/>
                    </div>
                    
                    <div id="welcome">
                        Welcome to Enterprise File Delivery <span class="build-version">2.2 (Java 7)</span> (<span class="build-date">Built on: March 17th, 2016</span>
			 <span class="build-number">#1</span>)
                    </div>
                    
                    <div id="dod_banner">
                        <p>You are accessing a U.S. Government (USG) information system (IS) that is provided for USG-authorized use only.</p>
                        <p>By using this IS (which includes any device attached to this IS), you consent to the following conditions:</p>
                        <ul>
                            <li>The USG routinely intercepts and monitors communications on this IS for purposes including, but not limited to, penetration testing, COMSEC monitoring, network operations and defense, personnel misconduct (PM), law enforcement (LE), and counterintelligence (CI) investigations.</li>
                            <li>At any time, the USG may inspect and seize data stored on this IS.</li>
                            <li>Communications using, or data stored on, this IS are not private, are subject to routine monitoring, interception, and search, and may be disclosed or used for any USG authorized purpose.</li>
                            <li>This IS includes security measures (e.g., authentication and access controls) to protect USG interests--not for your personal benefit or privacy.</li>
                            <li>Notwithstanding the above, using this IS does not constitute consent to PM, LE or CI investigative searching or monitoring of the content of privileged communications, or work product, related to personal representation or services by attorneys, psychotherapists, or clergy, and their assistants. Such communications and work product are private and confidential. See User Agreement for details.</li>
                        </ul>
                    </div>
            
                    <div id="cron" class="help">
                        <h3>Writing Cron Expressions</h3>
                        <p>A cron expression is a string comprised of 6 or 7 fields separated by white space. Fields can contain any of the allowed values, along with various combinations of the allowed special characters for that field. The fields are as follows:</p>
                        
                        <table class="format">
                             <tr>
                                 <th>Field Name</th>
                                 <th class="middle">Allowed Values</th>
                                 <th>Allowed Special Characters</th>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Seconds</td>
                                 <td class="middle">0-59</td>
                                 <td>, - * /</td>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Minutes</td>
                                 <td class="middle">0-59</td>
                                 <td>, - * /</td>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Hours</td>
                                 <td class="middle">0-23</td>
                                 <td>, - * /</td>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Day-of-month</td>
                                 <td class="middle">1-31</td>
                                 <td>, - * ? / L W</td>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Month</td>
                                 <td class="middle">1-12 or JAN-DEC</td>
                                 <td>, - * /</td>
                             </tr>
                             
                             <tr class="middle">
                                 <td>Day-of-Week</td>
                                 <td class="middle">1-7 or SUN-SAT</td>
                                 <td>, - * ? / L #</td>
                             </tr>
                             
                             <tr>
                                 <td>Year (Optional)</td>
                                 <td class="middle">empty, 1970-2099</td>
                                 <td>, - * /</td>
                             </tr>
                         </table>
                         
                         <h4>Special Characters</h4>
                         <ul>
                            <li>The '*' character is used to specify all values. For example, "*" in the minute field means "every minute".</li>
                            <li>The '?' character is allowed for the day-of-month and day-of-week fields. It is used to specify 'no specific value'. This is useful when you need to specify something in one of the two fileds, but not the other.</li>
                            <li>The '-' character is used to specify ranges For example "10-12" in the hour field means "the hours 10, 11 and 12".</li>
                            <li>The ',' character is used to specify additional values. For example "MON,WED,FRI" in the day-of-week field means "the days Monday, Wednesday, and Friday".</li>
                            <li>The '/' character is used to specify increments. For example "0/15" in the seconds field means "the seconds 0, 15, 30, and 45". And "5/15" in the seconds field means "the seconds 5, 20, 35, and 50". Specifying '*' before the '/' is equivalent to specifying 0 is the value to start with. Essentially, for each field in the expression, there is a set of numbers that can be turned on or off. For seconds and minutes, the numbers range from 0 to 59. For hours 0 to 23, for days of the month 0 to 31, and for months 1 to 12. The "/" character simply helps you turn on every "nth" value in the given set. Thus "7/6" in the month field only turns on month "7", it does NOT mean every 6th month, please note that subtlety.</li>
                            <li>The 'L' character is allowed for the day-of-month and day-of-week fields. This character is short-hand for "last", but it has different meaning in each of the two fields. For example, the value "L" in the day-of-month field means "the last day of the month" - day 31 for January, day 28 for February on non-leap years. If used in the day-of-week field by itself, it simply means "7" or "SAT". But if used in the day-of-week field after another value, it means "the last xxx day of the month" - for example "6L" means "the last friday of the month". When using the 'L' option, it is important not to specify lists, or ranges of values, as you'll get confusing results.</li>
                            <li>The 'W' character is allowed for the day-of-month field. This character is used to specify the weekday (Monday-Friday) nearest the given day. As an example, if you were to specify "15W" as the value for the day-of-month field, the meaning is: "the nearest weekday to the 15th of the month". So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. However if you specify "1W" as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not 'jump' over the boundary of a month's days. The 'W' character can only be specified when the day-of-month is a single day, not a range or list of days.</li>
                            <li>The 'L' and 'W' characters can also be combined for the day-of-month expression to yield 'LW', which translates to "last weekday of the month".</li>
                            <li>The '#' character is allowed for the day-of-week field. This character is used to specify "the nth" XXX day of the month. For example, the value of "6#3" in the day-of-week field means the third Friday of the month (day 6 = Friday and "#3" = the 3rd one in the month). Other examples: "2#1" = the first Monday of the month and "4#5" = the fifth Wednesday of the month. Note that if you specify "#5" and there is not 5 of the given day-of-week in the month, then no firing will occur that month.</li>
                            <li>The legal characters and the names of months and days of the week are not case sensitive.</li>
                        </ul>
                        
                        <h4>Notes</h4>
                        <ul class="notes">
                            <li>Support for specifying both a day-of-week and a day-of-month value is not complete (you'll need to use the '?' character in one of these fields).</li>
                        </ul>
                    </div>
                                    
                    <div id="admin-help" class="help">
                        <h3>Administrator Help</h3>
                        <h4>Users</h4>
                        <dl>
                            <dt>Username</dt><dd>The login handle of a particular user.</dd>
                            <dt>Enabled</dt><dd>Controls whether the user may log in.</dd>
                            <dt>Administrator</dt><dd>Determines if the user may administrate users and security policy, or manage content.</dd>
                            <dt>First Name</dt><dd>The first name of the user.</dd>
                            <dt>Last Name</dt><dd>The last name of the user.</dd>
                            <dt>Last Login</dt><dd>The last recorded log in.</dd>
                            <dt>Failed Logins</dt><dd>The number of failed log ins for a user.  This number is reset to 0 when a user succesfully logs in.</dd>
                        </dl>
                        
                        <h4>Allowed Hosts</h4>
                        <p>Allowed hosts are pre-configured certificate common names that Suppliers will use to control access for Consumers.  Only Administrators may add allowed hosts, however it is up to the user to choose which hosts apply to a Supplier.</p>
                        <dl>
                            <dt>Alias</dt><dd>The "friendly name," viewable and configured by the user.</dd>
                            <dt>Common Name</dt><dd>The common name from the certificate to be trusted.</dd>
                        </dl>
                    </div>
                        
                    <div id="user-help" class="help">
                        <h3>Supplier Content Sets</h3>
                        <dl>
                            <dt>Name</dt><dd>The name of the Supplier.</dd>
                            <dt>Description</dt><dd>An optional description for the Supplier.</dd>
                            <dt>Content Set Directory</dt><dd>The items you wish to supply for replication and synchronization.</dd>
                            <dt>Scheduling</dt><dd>The periodicity of snapshots taken of the file system.</dd>
                            <dt>Allowed Hosts</dt><dd>The aliases of other EFD servers that are allowed to consume from this supplier.  This is a whitelist; available entries are configured by an EFD administrator.</dd>
                            <dt>Support GBS</dt><dd>Check if this Supplier is to support transfers over GBS.  This will add extra configuration options.</dd>
                        </dl>
                        
                        <h4>SBM Information</h4>
                        <dl>
                            <dt>Name</dt><dd>If available, some SBM host and port information is preconfigured.  It may be selected here via a unique name.</dd>
                            <dt>Host</dt><dd>The IP address or host name of the SBM.</dd>
                            <dt>Port</dt><dd>The port to use for communications to the SBM.</dd>
                            <dt>Username</dt><dd>Username login credentials.</dd>
                            <dt>Password</dt><dd>Password for the supplier username.</dd>
                            <dt>Directory</dt><dd>Location to place items requested through GBS.</dd>
                            <dt>Passive Mode</dt><dd>Enable passive mode transfers.</dd>
                        </dl>
                        
                        <h4>Supplier Status</h4>
                        <ul>
                            <li>Provides information about current synchronizations.</li>
                            <li>Provides information about the inventory generation schedule.</li>
                            <li>Displays the current status of a content set.</li>
                        </ul>
                        
                        <h4>Supplier History</h4>
                        <ul>
                            <li>Reports the history of supplied data.</li>
                        </ul>
                        
                        <br/><br/>
                        
                        <h3>Consumer Content Sets</h3>
                        
                        <dl>
                            <dt>Name</dt><dd>The name of the Consumer.</dd>
                            <dt>Description</dt><dd>An optional description for the Consumer.</dd>
                            <dt>Content Set Directory</dt><dd>The location on the server where consumed items will be placed.</dd>
                            <dt>Scheduling</dt><dd>The periodicity of updates, to be polled from the Supplier.</dd>
                        </dl>
                        
                        <h4>Supplier Information</h4>
                        <dl>
                            <dt>Content Set Name</dt><dd>The name of the Supplier with which to synchronize.</dd>
                            <dt>Host Name or IP Address</dt><dd>The location of the Supplier.</dd>
                            <dt>Port</dt><dd>The HTTPS transport port number.</dd>
                        </dl>
                        
                        <h4>Retrieval Options</h4>
                        <dl>
                            <dt>Maximum File Size</dt><dd>The largest files to allow to be transferred.</dd>
                            <dt>File Delete Delay</dt><dd>The time to wait before deleting files.  A value of '0' means as soon as possible.</dd>
                            <dt>File GBS Transport</dt><dd>Check if the Supplier will use GBS transport.</dd>
                        </dl>
                        
                        <h4>Filter Options</h4>
                        <ul>
                            <li>Filter options allow more detailed choices regarding which files will be synchronized.</li>
                            <li>Even on Windows systems, these filters remain case sensitive.</li>
                            <li>Filters apply to both files and directories.</li>  
                            <li>Only a single filter may be specified.</li>
                        </ul>
                        <dl>
                            <dt>Accept only</dt><dd>Accept or include only files/directories that match the pattern.</dd>
                            <dt>Exclude</dt><dd>Exclude all files/directories that match the pattern.</dd>   
                        </dl>
                        
                        <h4>Consumer Status</h4>
                        <ul>
                            <li>Provides information about the next or current synchronization.</li>
                            <li>Displays the current status of a content set.</li>
                            <li>Displays the transfer metrics of a content set.</li>
                        </ul>
                        
                        <h4>Consumer History</h4>
                        <ul>
                            <li>Provides information about the history of synchronizations.</li>
                            <li>Displays metrics about each historic synchronization.</li>
                            <li>Displays the items modified with each historic synchronization.</li>
                        </ul>
                        
                        <h4>Dashboard</h4>
                        <ul>
                            <li>Displays active supplier and consumer synchronizations</li>
                        </ul>
                        
                        <h4>Notes</h4>
                        <ul class="notes">
                            <li>"Items" refers to both files and directories.</li>
                        </ul>
                    </div>

                </div>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
