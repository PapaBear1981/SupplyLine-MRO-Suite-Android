package com.example.supplyline_mro_suite.data.repository

import com.example.supplyline_mro_suite.data.local.dao.ToolDao
import com.example.supplyline_mro_suite.data.local.dao.UserDao
import com.example.supplyline_mro_suite.data.model.Tool
import com.example.supplyline_mro_suite.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataInitializer @Inject constructor(
    private val toolDao: ToolDao,
    private val userDao: UserDao
) {

    fun initializeSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if data already exists
            val toolCount = toolDao.getToolCount()
            val userCount = userDao.getUserCount()
            
            if (toolCount == 0) {
                insertSampleTools()
            }
            
            if (userCount == 0) {
                insertSampleUsers()
            }
        }
    }

    private suspend fun insertSampleTools() {
        val currentDateTime = LocalDateTime.now().toString()
        val sampleTools = listOf(
            Tool(
                id = 1,
                toolNumber = "HT001",
                serialNumber = "SN123456",
                description = "Torque Wrench 1/2\" Drive",
                category = "General",
                location = "Tool Crib A - Shelf 1",
                status = "Available",
                condition = "Good",
                notes = "Calibrated monthly",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(30).toString(),
                calibrationIntervalDays = 90
            ),
            Tool(
                id = 2,
                toolNumber = "HT002",
                serialNumber = "SN123457",
                description = "Drill Set Complete with Bits",
                category = "General",
                location = "Tool Crib A - Shelf 2",
                status = "Checked Out",
                condition = "Good",
                notes = "Complete set with case",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = false
            ),
            Tool(
                id = 3,
                toolNumber = "CL001",
                serialNumber = "SN123458",
                description = "Hydraulic Jack 20 Ton",
                category = "CL415",
                location = "Hangar 1 - Bay A",
                status = "Available",
                condition = "Excellent",
                notes = "CL415 specific equipment",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(60).toString(),
                calibrationIntervalDays = 180
            ),
            Tool(
                id = 4,
                toolNumber = "ENG001",
                serialNumber = "SN123459",
                description = "Engine Hoist 2000 lbs",
                category = "Engine",
                location = "Engine Shop - Bay 1",
                status = "Maintenance",
                condition = "Fair",
                notes = "Scheduled maintenance in progress",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = false
            ),
            Tool(
                id = 5,
                toolNumber = "SM001",
                serialNumber = "SN123460",
                description = "Pneumatic Rivet Gun",
                category = "Sheetmetal",
                location = "Sheetmetal Shop - Station 3",
                status = "Available",
                condition = "Good",
                notes = "Recently serviced",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = false
            ),
            Tool(
                id = 6,
                toolNumber = "HT003",
                serialNumber = "SN123461",
                description = "Digital Multimeter",
                category = "General",
                location = "Electronics Lab - Bench 2",
                status = "Available",
                condition = "Excellent",
                notes = "High precision instrument",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(45).toString(),
                calibrationIntervalDays = 365
            ),
            Tool(
                id = 7,
                toolNumber = "RJ001",
                serialNumber = "SN123462",
                description = "Avionics Test Set",
                category = "RJ85",
                location = "Avionics Shop - Test Station 1",
                status = "Checked Out",
                condition = "Good",
                notes = "RJ85 specific test equipment",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().minusDays(5).toString(), // Overdue
                calibrationIntervalDays = 180
            ),
            Tool(
                id = 8,
                toolNumber = "Q001",
                serialNumber = "SN123463",
                description = "Borescope Inspection Kit",
                category = "Q400",
                location = "NDT Lab - Cabinet A",
                status = "Available",
                condition = "Good",
                notes = "Q400 engine inspection kit",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = false
            ),
            Tool(
                id = 9,
                toolNumber = "CNC001",
                serialNumber = "SN123464",
                description = "Precision Measuring Set",
                category = "CNC",
                location = "Machine Shop - Tool Cabinet",
                status = "Available",
                condition = "Excellent",
                notes = "High precision measurement tools",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(15).toString(),
                calibrationIntervalDays = 90
            ),
            Tool(
                id = 10,
                toolNumber = "HT004",
                serialNumber = "SN123465",
                description = "Impact Wrench Set",
                category = "General",
                location = "Tool Crib B - Shelf 1",
                status = "Available",
                condition = "Good",
                notes = "Various socket sizes included",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = false
            ),
            Tool(
                id = 11,
                toolNumber = "CL002",
                serialNumber = "SN123466",
                description = "Wing Jack CL415",
                category = "CL415",
                location = "Hangar 1 - Bay B",
                status = "Available",
                condition = "Good",
                notes = "CL415 wing support equipment",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(90).toString(),
                calibrationIntervalDays = 365
            ),
            Tool(
                id = 12,
                toolNumber = "ENG002",
                serialNumber = "SN123467",
                description = "Compression Tester",
                category = "Engine",
                location = "Engine Shop - Bay 2",
                status = "Available",
                condition = "Excellent",
                notes = "Engine compression testing kit",
                createdAt = currentDateTime,
                updatedAt = currentDateTime,
                requiresCalibration = true,
                calibrationDueDate = LocalDate.now().plusDays(120).toString(),
                calibrationIntervalDays = 180
            )
        )

        toolDao.insertTools(sampleTools)
    }

    private suspend fun insertSampleUsers() {
        val currentDateTime = LocalDateTime.now().toString()
        val sampleUsers = listOf(
            User(
                id = 1,
                employeeNumber = "EMP001",
                name = "John Smith",
                department = "Maintenance",
                isAdmin = false,
                createdAt = currentDateTime,
                lastLogin = currentDateTime,
                isActive = true
            ),
            User(
                id = 2,
                employeeNumber = "EMP002",
                name = "Sarah Johnson",
                department = "Avionics",
                isAdmin = false,
                createdAt = currentDateTime,
                lastLogin = currentDateTime,
                isActive = true
            ),
            User(
                id = 3,
                employeeNumber = "EMP003",
                name = "Mike Wilson",
                department = "Engine Shop",
                isAdmin = true,
                createdAt = currentDateTime,
                lastLogin = currentDateTime,
                isActive = true
            ),
            User(
                id = 4,
                employeeNumber = "EMP004",
                name = "Lisa Brown",
                department = "Sheetmetal",
                isAdmin = false,
                createdAt = currentDateTime,
                lastLogin = currentDateTime,
                isActive = true
            ),
            User(
                id = 5,
                employeeNumber = "EMP005",
                name = "David Lee",
                department = "Quality Control",
                isAdmin = false,
                createdAt = currentDateTime,
                lastLogin = currentDateTime,
                isActive = true
            )
        )

        userDao.insertUsers(sampleUsers)
    }
}
