using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

using System.Web;
using System.Collections;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;

namespace GSBControlPanel
{
    public partial class frmWebApplicationList : Form
    {
        const string PATH = "path";
        const string SLASH = "\\";
        const string WEB_CONFIG_FILE = "web.config";
        public string myBasePath = "";
        private bool isInstaller = false;
        TableLayoutPanel tlpWebAppEntry = new TableLayoutPanel();

        public frmWebApplicationList(string inMyBasePath, bool bInstaller)
        {
            InitializeComponent();
            
            isInstaller = bInstaller;
            myBasePath = inMyBasePath;
            
            flowLayoutPanel1.Visible = true;
            flowLayoutPanel1.Enabled = true;
            flowLayoutPanel1.FlowDirection = FlowDirection.TopDown;
            flowLayoutPanel1.Controls.Clear();//remove previous controls

                        
            tlpWebAppEntry.GrowStyle = TableLayoutPanelGrowStyle.AddRows;// If grid is full add extra cells by adding row
            tlpWebAppEntry.Padding = new Padding(1, 1, 4, 5);// Padding (pixels)within each cell (left, top, right, bottom)
            tlpWebAppEntry.AutoSize = true;// Resize the TableLayoutPanel
            tlpWebAppEntry.Name = "tttableLayoutPanel1";
            tlpWebAppEntry.Width = 40;
            tlpWebAppEntry.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;

            if (isInstaller == false)
            {
                tlpWebAppEntry.ColumnCount = 3;
            }
            else
            {
                tlpWebAppEntry.ColumnCount = 2;
            }
            tlpWebAppEntry.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            tlpWebAppEntry.Controls.Clear();

            GetAllSiteCollectionFromAllWebApps();//detect and add the web applications list on load
            
            flowLayoutPanel1.Controls.Add(tlpWebAppEntry);//add controls to the panel
        }

        
        
        public void SaveAllWebAppConfigurationsToFile(GSAConfiguration gc)
        {
            string webConfigFilePath = null;
            foreach (Control c in flowLayoutPanel1.Controls)
            {
                TableLayoutPanel tlb = c as TableLayoutPanel;
                if (tlb != null)
                {
                    foreach (Control child1 in tlb.Controls)
                    {
                        #region update all checked web applications
                        Label PathLabel = child1 as Label;

                        //In case multiple labels are added
                        if ((PathLabel != null) && (PathLabel.Name.Equals(PATH)))
                        {
                            if (!PathLabel.Text.EndsWith(SLASH))
                            {
                                webConfigFilePath = PathLabel.Text + SLASH + WEB_CONFIG_FILE;
                            }
                            else
                            {
                                webConfigFilePath = PathLabel.Text + WEB_CONFIG_FILE;
                            }

                            //SaveConfigurationsToFile(gc, webConfigFilePath);
                            gc.SaveConfigurationsToFile(webConfigFilePath, true); //change the settings of the web application as per the "GSA Settings panel"
                                                                                    //This will always be called during installation
                            
                        }
                        #endregion update all checked web applications

                    }//end: foreach#1
                }

            }

            this.Close();//close the form
        }

        /// <summary>
        /// Creates controls for the web application to be added
        /// </summary>
        /// <param name="wa">Web Application to be added</param>
        private void AddNewWebApplication(SPWebApplication wa, int i)
        {
            String WebAppName = GetWebAppName(wa);
            GSBLabel lblWebAppName = new GSBLabel(WebAppName);
            GSBLabel lblPath = new GSBLabel(wa.IisSettings[0].Path.ToString());//port

            lblPath.Name = PATH;
            tlpWebAppEntry.Controls.Add(lblWebAppName, 0, i);
            tlpWebAppEntry.Controls.Add(lblPath, 1, i);

            //create the additional "Edit" button for editing the individual web app config for GSA
            if (isInstaller == false)
            {
                GSBEditAppButton btnEdit = new GSBEditAppButton();
                btnEdit.WebConfigPath = wa.IisSettings[0].Path.ToString();
                tlpWebAppEntry.Controls.Add(btnEdit, 2, i);
            }
            
        }


        /// <summary>
        /// Detect all the web applications for a given sharepoint installation (machine)
        /// </summary>
        public void GetAllSiteCollectionFromAllWebApps()
        {
            int i = -1;
            bool isNoWebApps = true;
            try
            {
                //get the site collection for the central administration
                foreach (SPWebApplication wa in SPWebService.AdministrationService.WebApplications)
                {
                    try
                    {
                        //Get the alternate URls
                        SPAlternateUrlCollection CentralAdminAlternateURLCollection = wa.AlternateUrls;//Find all the alternal URL mapping for the central admin web application
                        /*
                         * Logic: if any of the URLs belong to the current machine then include the central admin
                         * web application in our current list
                         * else, ignore it
                         */

                        bool IsLookForMoreWebs = true;
                                                    
                        if (null != CentralAdminAlternateURLCollection)
                        {
                            foreach (SPAlternateUrl CentralWebApplnAlternateURL in CentralAdminAlternateURLCollection)
                            {
                                if ((null != CentralWebApplnAlternateURL) && (IsLookForMoreWebs==true))
                                {
                                    Uri uri = CentralWebApplnAlternateURL.Uri;
                                    if (null != uri)
                                    {
                                        string fqdnHost = uri.Host;//Get teh 
                                        char[] separator = { '.' };
                                        string onlyhost = fqdnHost.Split(separator)[0];

                                        string computerName = SystemInformation.ComputerName.ToLower();
                                        if ((null != onlyhost) && (onlyhost.ToLower().Equals(computerName)))
                                        {
                                            AddNewWebApplication(wa, ++i); //Note: Central web application needs to be added always
                                            isNoWebApps = false;
                                            IsLookForMoreWebs = false;
                                        }
                                    }

                                   
                                }
                            }
                        }

                        ///////////////////////
                    }
                    catch {}
                }

                foreach (SPWebApplication wa in SPWebService.ContentService.WebApplications)
                {
                    try
                    {
                        //need to check if the web application are from the same machine.useful for farm scenario
                        if (GSBInstaller.isLocalWebApplication(wa))
                        {
                            AddNewWebApplication(wa,++i);
                            isNoWebApps = false;
                        }
                    }catch{}
                }
            }
            catch (Exception)
            {
                //MessageBox.Show(e.StackTrace.ToString(), e.Message);
            }

            //handling for blank web apps
            if (isNoWebApps == true)
            { 
                /*
                 * 1. change the display message
                 * 2. disable the OK button.. user should not be allowed to proceed further
                 */
                lblWarning.Text = "No local SharePoint web applications found in this machine";
                button2.Enabled = false;
            }

        }

        /// <summary>
        /// Get the Web application Display name 
        /// </summary>
        private string GetWebAppName(SPWebApplication wa)
        {
            if ((wa.Name != null) && (!wa.Name.Trim().Equals("")))
            {
                return wa.Name;
            }
            else
            {
                return wa.DefaultServerComment;//DefaultServerComment = "SharePoint Central Administration v3"
            }

        }

       
         private void button2_Click_1(object sender, EventArgs e)
        {
            if (isInstaller == true)
            {
                this.Visible = false;//hide the form
                                    //retain the form as it contains the information about all web applications

                frmGSAParams GSAParamForm = new frmGSAParams();
                DialogResult result = GSAParamForm.ShowDialog();

                //check if OK was clicked
                if(result == DialogResult.OK)
                {
                    GSAConfiguration gc = GSAParamForm.PopulateGSAConfiguration(myBasePath);
                    SaveAllWebAppConfigurationsToFile(gc);//save the form
                }
            }

            this.DialogResult = DialogResult.OK;//indicates that OK is clicked
            this.Close();//close the form
        }
               
        private void btnCancel_Click_1(object sender, EventArgs e)
        {
            
            this.DialogResult = DialogResult.Cancel;//indicates cancel
            this.Close();//close the form..initiate terminate the installer
        }

        private void frmWebApplicationList_Load(object sender, EventArgs e)
        {
            if (isInstaller == false)
            {
                btnCancel.Visible = false;
                button2.Text = "&Ok";
                button2.SetBounds(button2.Location.X+26, button2.Location.Y, button2.Width+4, button2.Height+3);
                pictureBox1.Visible = false;
                lblWarning.Text = "Following is the list of SharePoint web applications. Select Edit to change the Google Search Appliance parameters for a given web application";
                lblWarning.SetBounds(lblWarning.Location.X - 8, lblWarning.Location.Y, lblWarning.Width, lblWarning.Height);
            }
        }

       
    }
}