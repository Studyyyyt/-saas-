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
import Patient360View from "@/views/Manager/Patient360View.vue";
import MedicalRecordView from "@/views/Manager/MedicalRecordView.vue";
import FollowupManagementView from "@/views/Manager/FollowupManagementView.vue";
import ConsultationView from "@/views/Manager/ConsultationView.vue";
import ConsultationDashboardView from "@/views/Manager/ConsultationDashboardView.vue";
import AdvertisingSpendingView from "@/views/Manager/AdvertisingSpendingView.vue";
import BindSuccess from "@/views/BindSuccess.vue";
import AppointmentNoticeView from "@/views/AppointmentNoticeView.vue";
import PatientPortalHome from "@/views/PatientPortalHome.vue";
import PatientPortalAuthError from "@/views/PatientPortalAuthError.vue";
import PatientRegisterH5 from "@/views/PatientRegisterH5.vue";
import StaffPortalHome from "@/views/StaffPortalHome.vue";
import StaffPortalAuthError from "@/views/StaffPortalAuthError.vue";
import StaffPortalBind from "@/views/StaffPortalBind.vue";
import StaffAppointmentH5 from "@/views/StaffAppointmentH5.vue";
import StaffConsultationH5 from "@/views/StaffConsultationH5.vue";
import StaffPatientH5 from "@/views/StaffPatientH5.vue";
import StaffPatient360H5 from "@/views/StaffPatient360H5.vue";
import StaffMedicalRecordH5 from "@/views/StaffMedicalRecordH5.vue";
import StaffFinanceH5 from "@/views/StaffFinanceH5.vue";
import StaffInventoryH5 from "@/views/StaffInventoryH5.vue";
import StaffLabProcessingH5 from "@/views/StaffLabProcessingH5.vue";
import StaffMaterialPurchaseH5 from "@/views/StaffMaterialPurchaseH5.vue";
import AdminReportH5 from "@/views/AdminReportH5.vue";
import PatientPortalSection from "@/views/PatientPortalSection.vue";
import StaffH5Shell from "@/views/StaffH5Shell.vue";
import TreatmentCatalogView from "@/views/Manager/TreatmentCatalogView.vue";
import InsuranceOverviewView from "@/views/Manager/InsuranceOverviewView.vue";
import InsuranceConfigView from "@/views/Manager/InsuranceConfigView.vue";
import InsurancePatientProfileView from "@/views/Manager/InsurancePatientProfileView.vue";
import InsuranceSettlementView from "@/views/Manager/InsuranceSettlementView.vue";
import InsuranceLogView from "@/views/Manager/InsuranceLogView.vue";
import InsuranceMockPayloadView from "@/views/Manager/InsuranceMockPayloadView.vue";
import BusinessAnalysisView from "@/views/Manager/BusinessAnalysisView.vue";
import SystemTreatmentCatalogView from "@/views/Manager/SystemTreatmentCatalogView.vue";
import SystemPaymentChannelView from "@/views/Manager/SystemPaymentChannelView.vue";
import SystemConsentTemplateView from "@/views/Manager/SystemConsentTemplateView.vue";
import SystemAccountPermissionView from "@/views/Manager/SystemAccountPermissionView.vue";
import SystemAccountManageView from "@/views/Manager/SystemAccountManageView.vue";
import SystemSettingsView from "@/views/Manager/SystemSettingsView.vue";
import AIAgentConfigView from "@/views/Manager/AIAgentConfigView.vue";
import ModelProviderConfigView from "@/views/Manager/ModelProviderConfigView.vue";
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
import {
  getPatientPortalQuery,
  getPatientPortalSession,
  getStaffPortalQuery,
  getStaffPortalSession,
  savePatientPortalSessionFromQuery,
  saveStaffPortalSessionFromQuery
} from "@/utils/portalSession";

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
      { path: 'BusinessAnalysis', name: 'BusinessAnalysisView', component: BusinessAnalysisView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemTreatmentCatalog', name: 'SystemTreatmentCatalogView', component: SystemTreatmentCatalogView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemPaymentChannel', name: 'SystemPaymentChannelView', component: SystemPaymentChannelView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemConsentTemplate', name: 'SystemConsentTemplateView', component: SystemConsentTemplateView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemAccountPermission', name: 'SystemAccountPermissionView', component: SystemAccountPermissionView, meta: { allowedRoles: ['admin'] }},
      { path: 'SystemAccountManage', name: 'SystemAccountManageView', component: SystemAccountManageView, meta: { allowedRoles: ['admin'] }},
      { path: 'SystemSettings', name: 'SystemSettingsView', component: SystemSettingsView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemAIAgentConfig', name: 'AIAgentConfigView', component: AIAgentConfigView, meta: { allowedRoles: ['admin', 'nurse'] }},
      { path: 'SystemModelProviderConfig', name: 'ModelProviderConfigView', component: ModelProviderConfigView, meta: { allowedRoles: ['admin', 'nurse'] }},
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
      { path: 'Patient360',name: 'Patient360View',component: Patient360View},
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
  },
  {
    path: '/app/bind-success',
    name: 'BindSuccess',
    component: BindSuccess
  },
  {
    path: '/appointment-notice',
    name: 'AppointmentNoticeView',
    component: AppointmentNoticeView
  },
  {
    path: '/patient-register-h5',
    name: 'PatientRegisterH5',
    component: PatientRegisterH5
  },
  {
    path: '/patient-portal-home',
    name: 'PatientPortalHome',
    component: PatientPortalHome
  },
  {
    path: '/patient-portal-section',
    name: 'PatientPortalSection',
    component: PatientPortalSection
  },
  {
    path: '/portal-auth-error',
    name: 'PatientPortalAuthError',
    component: PatientPortalAuthError
  },
  {
    path: '/staff-portal-home',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffPortalHome',
        component: StaffPortalHome
      }
    ]
  },
  {
    path: '/staff-h5/appointments',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffAppointmentH5',
        component: StaffAppointmentH5
      }
    ]
  },
  {
    path: '/staff-h5/consultations',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffConsultationH5',
        component: StaffConsultationH5
      }
    ]
  },
  {
    path: '/staff-h5/patients',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffPatientH5',
        component: StaffPatientH5
      }
    ]
  },
  {
    path: '/staff-h5/patient360',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffPatient360H5',
        component: StaffPatient360H5
      }
    ]
  },
  {
    path: '/staff-h5/records',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffMedicalRecordH5',
        component: StaffMedicalRecordH5
      }
    ]
  },
  {
    path: '/staff-h5/finance',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffFinanceH5',
        component: StaffFinanceH5
      }
    ]
  },
  {
    path: '/staff-h5/inventory',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffInventoryH5',
        component: StaffInventoryH5
      }
    ]
  },
  {
    path: '/staff-h5/lab-processing',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffLabProcessingH5',
        component: StaffLabProcessingH5
      }
    ]
  },
  {
    path: '/staff-h5/material-purchases',
    component: StaffH5Shell,
    children: [
      {
        path: '',
        name: 'StaffMaterialPurchaseH5',
        component: StaffMaterialPurchaseH5
      }
    ]
  },
  {
    path: '/staff-portal-bind',
    name: 'StaffPortalBind',
    component: StaffPortalBind
  },
  {
    path: '/staff-portal-auth-error',
    name: 'StaffPortalAuthError',
    component: StaffPortalAuthError
  },
  {
    path: '/admin-report-h5',
    name: 'AdminReportH5',
    component: AdminReportH5
  }


  // {
  //   path: '/about',
  //   name: 'about',
  //   // route level code-splitting
  //   // this generates a separate chunk (about.[hash].js) for this route
  //   // which is lazy-loaded when the route is visited.
  //   component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  // }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

const PUBLIC_PATHS = new Set([
  '/login1',
  '/register',
  '/app/bind-success',
  '/appointment-notice',
  '/patient-register-h5',
  '/portal-auth-error',
  '/staff-portal-bind',
  '/staff-portal-auth-error',
  '/admin-report-h5'
])

function isPatientPortalPath(path) {
  return path === '/patient-portal-home' || path === '/patient-portal-section'
}

function isStaffPortalPath(path) {
  return path === '/staff-portal-home' || path.startsWith('/staff-h5/')
}

router.beforeEach((to, from, next) => {
  if (isPatientPortalPath(to.path)) {
    savePatientPortalSessionFromQuery(to.query)
    const session = getPatientPortalSession()
    if (!session) {
      next({ path: '/portal-auth-error', query: { reason: 'session' } })
      return
    }
    const normalizedQuery = getPatientPortalQuery(to.query)
    if (String(to.query.patientId || '') !== String(normalizedQuery.patientId || '')
      || String(to.query.portalToken || '') !== String(normalizedQuery.portalToken || '')) {
      next({ path: to.path, query: normalizedQuery, replace: true })
      return
    }
    next()
    return
  }

  if (isStaffPortalPath(to.path)) {
    saveStaffPortalSessionFromQuery(to.query)
    const session = getStaffPortalSession()
    if (!session) {
      next({ path: '/staff-portal-auth-error', query: { reason: 'session' } })
      return
    }
    const normalizedQuery = getStaffPortalQuery(to.query)
    if (String(to.query.accountId || '') !== String(normalizedQuery.accountId || '')
      || String(to.query.staffToken || '') !== String(normalizedQuery.staffToken || '')) {
      next({ path: to.path, query: normalizedQuery, replace: true })
      return
    }
    next()
    return
  }

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
