package com.example.anyquestion.questioner

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuestionerRepository : JpaRepository<Questioner, Int>
{
}