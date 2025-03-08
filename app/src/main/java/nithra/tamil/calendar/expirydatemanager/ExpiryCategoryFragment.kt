package nithra.tamil.calendar.expirydatemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import nithra.tamil.calendar.expirydatemanager.databinding.FragmentCategoryBinding
import nithra.tamil.calendar.expirydatemanager.databinding.FragmentExpiryCategoryBinding

class ExpiryCategoryFragment : Fragment() {

    private var _binding: FragmentExpiryCategoryBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpiryCategoryBinding.inflate(inflater, container, false)

        // Fetch expiry item categories
        val categories = (activity as? Category)?.fetchCategories("Expiry Item") ?: emptyList()

        // Display categories in ListView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        binding.listViewCategories.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
