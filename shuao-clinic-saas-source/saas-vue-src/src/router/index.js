import Vue from 'vue'
import VueRouter from 'vue-router'
import Manager from '../views/Manager.vue'
import InventoryView from "@/views/Manager/InventoryView.vue";
import InventoryView2 from "@/views/Manager/InventoryView2.vue";
import AppointmentView from "@/views/Manager/AppointmentView.vue";
import TreatmentView from "@/views/Manager/TreatmentView.vue";
import TreatmentView2 from "@/views/Manager/TreatmentView2.vue";
import FinancialView from "@/views/Manager/FinancialView.vue";
import DoctorView from "@/views/Manager/DoctorView.vue";
import AccountView from "@/views/Manager/AccountView.vue";
import FinancialView2 from "@/views/Manager/FinancialView2.vue";
import FinancialExpenseView from "@/views/Manager/FinancialExpenseView.vue";

import HomeView from "@/views/Manager/HomeView.vue";
import PersonView from "@/views/Manager/PersonView.vue";
import login1 from "@/views/login1.vue";
import register from "@/views/register.vue";

import PatientView from "@/views/Manager/PatientView.vue";
import InventoryView3 from "@/views/Manager/InventoryView3.vue";
import PatientDetailView from "@/views/Manager/PatientDetailView.vue";
import MedicalRecordView from "@/views/Manager/MedicalRecordView.vue";
import FollowupManagementView from "@/views/Manager/FollowupManagementView.vue";
import ConsultationView from "@/views/Manager/ConsultationView.vue";
import ConsultationDashboardView from "@/views/Manager/ConsultationDashboardView.vue";
import AdvertisingSpendingView from "@/views/Manager/AdvertisingSpendingView.vue";
import TreatmentCatalogView from "@/views/Manager/TreatmentCatalogView.vue";
import InsuranceOverviewView from "@/views/Manager/InsuranceOverviewView.vue";
import InsuranceConfigView from "@/views/Manager/InsuranceConfigView.vue";
import InsurancePatientProfileView from "@/views/Manager/InsurancePatientProfileView.vue";
import InsuranceSettlementView from "@/views/Manager/InsuranceSettlementView.vue";
import InsuranceLogView from "@/views/Manager/InsuranceLogView.vue";
import InsuranceMockPayloadView from "@/views/Manager/InsuranceMockPayloadView.vue";
import SystemTreatmentCatalogView from "@/views/Manager/SystemTreatmentCatalogView.vue";
import SystemPaymentChannelView from "@/views/Manager/SystemPaymentChannelView.vue";
import SystemConsentTemplateView from "@/views/Manager/SystemConsentTemplateView.vue";
import SystemAccountPermissionView from "@/views/Manager/SystemAccountPermissionView.vue";
import SystemAccountManageView from "@/views/Manager/SystemAccountManageView.vue";
import SystemSettingsView from "@/views/Manager/SystemSettingsView.vue";
import SystemSettingsLayout from "@/views/Manager/SystemSettingsLayout.vue";
import AIOverviewView from "@/views/Manager/AIOverviewView.vue";
import MedicalRecordAIConfigView from "@/views/Manager/MedicalRecordAIConfigView.vue";
import HelpDocumentLayout from "@/views/Manager/HelpDocumentLayout.vue";
import HelpDocumentIndexView from "@/views/Manager/HelpDocumentIndexView.vue";
import HelpMedicalRecordAIView from "@/views/Manager/HelpMedicalRecordAIView.vue";
import HelpTreatmentSceneView from "@/views/Manager/HelpTreatmentSceneView.vue";
import HelpPatientInsightView from "@/views/Manager/HelpPatientInsightView.vue";
import HelpModelProviderView from "@/views/Manager/HelpModelProviderView.vue";
import HelpAIOverviewView from "@/views/Manager/HelpAIOverviewView.vue";
import LabFactoryView from "@/views/Manager/LabFactoryView.vue";
import LabFactoryDetailView from "@/views/Manager/LabFactoryDetailView.vue";
import LabOrderView from "@/views/Manager/LabOrderView.vue";
import LabBillView from "@/views/Manager/LabBillView.vue";
import LabBillDetailView from "@/views/Manager/LabBillDetailView.vue";
import LabStatisticsView from "@/views/Manager/LabStatisticsView.vue";
import MaterialCategoryView from "@/views/Manager/MaterialCategoryView.vue";
import MaterialView from "@/views/Manager/MaterialView.vue";
import MaterialPurchaseView from "@/views/Manager/MaterialPurchaseView.vue";
import MaterialPurchaseDetailView from "@/views/Manager/MaterialPurchaseDetailView.vue";
import MaterialStatisticsView from "@/views/Manager/MaterialStatisticsView.vue";
import { getAdminSession, hasAdminSession } from "@/utils/adminSession";
import { findFirstAccessibleMenuKey, getAllowedMenuKeys, resolveMenuPermissionKey } from "@/utils/roleMenuCatalog";

Vue.use(VueRouter)

function normalizeRole(rawRole) {
  const role = String(rawRole || '').trim()
  if (role === '管理员' || role === 'admin') return 'admin'
  if (role === '医生' || role === 'doctor') return 'doctor'
  if (role === '护士' || role === 'nurse') return 'nurse'
  return role
}

function currentAdminRole() {
  const session = getAdminSession() || {}
  return normalizeRole(session.role)
}

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: Manager,
    redirect:'/login1',//重定向到login1
    children:[      //子路由
      { path: 'home', name: 'home', component: HomeView},
      { path: 'inventory', alias: ['/Inventory'], redirect: '/materials' },
      { path: 'Inventory2', redirect: '/material-statistics' },
      { path: 'Inventory3', redirect: '/material-purchases' },
      { path: 'Appointment', name: 'AppointmentView',component: AppointmentView},
      { path: 'Treatment',name: 'TreatmentView',component: TreatmentView},
      { path: 'Treatment2',name: 'TreatmentView2',component: TreatmentView2},
      { path: 'Financial', name: 'FinancialView',component: FinancialView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'Financial2',  name: 'FinancialView2',component: FinancialView2, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'financial-expenses', alias: ['/FinancialExpense'], name: 'FinancialExpenseView', component: FinancialExpenseView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'Doctor', name: 'DoctorView', component: DoctorView},
      { path: 'Account',name: 'AccountView',component: AccountView},
      { path: 'TreatmentCatalog', name: 'TreatmentCatalogView', component: TreatmentCatalogView},
      { path: 'InsuranceOverview', name: 'InsuranceOverviewView', component: InsuranceOverviewView},
      { path: 'InsuranceConfig', name: 'InsuranceConfigView', component: InsuranceConfigView},
      { path: 'InsurancePatientProfile', name: 'InsurancePatientProfileView', component: InsurancePatientProfileView},
      { path: 'InsuranceSettlement', name: 'InsuranceSettlementView', component: InsuranceSettlementView},
      { path: 'InsuranceLog', name: 'InsuranceLogView', component: InsuranceLogView},
      { path: 'InsuranceMockPayload', name: 'InsuranceMockPayloadView', component: InsuranceMockPayloadView},
      { path: 'SystemTreatmentCatalog', name: 'SystemTreatmentCatalogView', component: SystemTreatmentCatalogView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemPaymentChannel', name: 'SystemPaymentChannelView', component: SystemPaymentChannelView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemConsentTemplate', name: 'SystemConsentTemplateView', component: SystemConsentTemplateView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemAccountPermission', name: 'SystemAccountPermissionView', component: SystemAccountPermissionView, meta: { allowedRoles: ['admin'] }},
      { path: 'SystemAccountManage', name: 'SystemAccountManageView', component: SystemAccountManageView, meta: { allowedRoles: ['admin'] }},
      {
        path: 'SystemSettings',
        component: SystemSettingsLayout,
        meta: { allowedRoles: ['admin', 'nurse'] },
        redirect: '/SystemSettings/ai/overview',
        children: [
          { path: 'basic/treatment', component: SystemTreatmentCatalogView },
          { path: 'basic/payment', component: SystemPaymentChannelView },
          { path: 'basic/consent', component: SystemConsentTemplateView },
          { path: 'basic/account', component: SystemAccountManageView },
          { path: 'ai/overview', name: 'AIOverviewView', component: AIOverviewView, meta: { allowedRoles: ['admin'] } },
          {
            path: 'help',
            component: HelpDocumentLayout,
            redirect: '/SystemSettings/help/index',
            children: [
              { path: 'index', name: 'HelpDocumentIndexView', component: HelpDocumentIndexView },
              { path: 'ai/overview', component: HelpAIOverviewView },
              { path: 'ai/medical', component: HelpMedicalRecordAIView },
              { path: 'ai/scene', component: HelpTreatmentSceneView },
              { path: 'ai/patient', component: HelpPatientInsightView },
              { path: 'ai/model', component: HelpModelProviderView }
            ]
          }
        ]
      },
      { path: 'SystemSettingsOld', name: 'SystemSettingsView', component: SystemSettingsView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'lab-factories', alias: ['/LabFactory'], name: 'LabFactoryView', component: LabFactoryView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'lab-factories/:id', alias: ['/LabFactoryDetail/:id'], name: 'LabFactoryDetailView', component: LabFactoryDetailView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'lab-orders', alias: ['/LabOrder'], name: 'LabOrderView', component: LabOrderView, meta: { allowedRoles: ['admin', 'doctor', 'nurse'] } },
      { path: 'lab-bills', alias: ['/LabBill'], name: 'LabBillView', component: LabBillView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'lab-bills/:id', alias: ['/LabBillDetail/:id'], name: 'LabBillDetailView', component: LabBillDetailView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'lab-statistics', alias: ['/LabStatistics'], name: 'LabStatisticsView', component: LabStatisticsView, meta: { allowedRoles: ['admin'] } },
      { path: 'material-categories', alias: ['/MaterialCategory'], name: 'MaterialCategoryView', component: MaterialCategoryView, meta: { allowedRoles: ['admin'] } },
      { path: 'materials', alias: ['/Material'], name: 'MaterialView', component: MaterialView, meta: { allowedRoles: ['admin', 'doctor', 'nurse'] } },
      { path: 'material-purchases', alias: ['/MaterialPurchase'], name: 'MaterialPurchaseView', component: MaterialPurchaseView, meta: { allowedRoles: ['admin', 'doctor', 'nurse'] } },
      { path: 'material-purchases/:id', alias: ['/MaterialPurchaseDetail/:id'], name: 'MaterialPurchaseDetailView', component: MaterialPurchaseDetailView, meta: { allowedRoles: ['admin', 'doctor', 'nurse'] } },
      { path: 'material-statistics', alias: ['/MaterialStatistics'], name: 'MaterialStatisticsView', component: MaterialStatisticsView, meta: { allowedRoles: ['admin'] } },
      { path: 'Person',name: 'PersonView',component: PersonView},
      { path: 'Patient',name: 'PatientView',component: PatientView},
      { path: 'PatientDetail',name: 'PatientDetailView',component: PatientDetailView},
      { path: 'MedicalRecord',name: 'MedicalRecordView',component: MedicalRecordView},
      { path: 'Followup', name: 'FollowupManagementView', component: FollowupManagementView},
      { path: 'Consultation', name: 'ConsultationView', component: ConsultationView, meta: { allowedRoles: ['admin', 'doctor', 'nurse'] } },
      { path: 'ConsultationDashboard', name: 'ConsultationDashboardView', component: ConsultationDashboardView, meta: { allowedRoles: ['admin', 'nurse'] } },
      { path: 'advertising-spending', name: 'AdvertisingSpendingView', component: AdvertisingSpendingView, meta: { allowedRoles: ['admin', 'nurse'] } },
      // { path: 'login',name: 'login',component: login1},
    ]
  },
    {
    path: '/login1',
    name: 'login1',
    component: login1
  },
  {
    path: '/register',
    name: 'register',
    component: register
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

const PUBLIC_PATHS = new Set([
  '/login1',
  '/register'
])

router.beforeEach((to, from, next) => {
  if (!PUBLIC_PATHS.has(to.path) && !hasAdminSession()) {
    next({ path: '/login1', query: { reason: 'auth' } })
    return
  }

  const session = getAdminSession() || {}
  const menuPermissionKey = resolveMenuPermissionKey(to.path)
  const permissionsLoaded = session.roleMenuPermissionsLoaded === true && Array.isArray(session.allowedMenuKeys)
  const configuredAllowedKeys = permissionsLoaded ? getAllowedMenuKeys(session) : []
  if (menuPermissionKey && permissionsLoaded && !configuredAllowedKeys.includes(menuPermissionKey)) {
    next({ path: findFirstAccessibleMenuKey(session), replace: true })
    return
  }

  const restrictedRecord = [...to.matched].reverse().find(record => Array.isArray(record.meta && record.meta.allowedRoles))
  if (restrictedRecord && !permissionsLoaded) {
    const role = currentAdminRole()
    const allowedRoles = restrictedRecord.meta.allowedRoles
    if (!allowedRoles.includes(role)) {
      next({ path: role === 'doctor' ? '/Consultation' : '/home', replace: true })
      return
    }
  }

  next()
})

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(err => err)
}

export default router
